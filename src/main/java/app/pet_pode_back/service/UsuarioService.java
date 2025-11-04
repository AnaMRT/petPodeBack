package app.pet_pode_back.service;

import app.pet_pode_back.dto.UsuarioUpdateDTO;
import app.pet_pode_back.model.Pet;
import app.pet_pode_back.model.Plantas;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.PasswordResetTokenRepository;
import app.pet_pode_back.repository.PlantaRepository;
import app.pet_pode_back.repository.UsuarioRepository;

import app.pet_pode_back.model.PasswordResetToken;
import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import jakarta.validation.Valid;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;


import java.util.List;

@Service
public class UsuarioService {


    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private PlantaRepository plantaRepository;
    @Autowired
    private PasswordResetTokenRepository resetTokenRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;



    public Usuario cadastrar(@Valid Usuario usuario) {
        String senhaCriptografada = passwordEncoder.encode(usuario.getSenha());
        usuario.setSenha(senhaCriptografada);
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario editarUsuario(UUID usuarioId, UsuarioUpdateDTO dto) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));


        if (dto.getNome() != null && !dto.getNome().isBlank()) {
            usuario.setNome(dto.getNome());
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            usuario.setEmail(dto.getEmail());
        }

        if (dto.getSenhaAtual() != null && !dto.getSenhaAtual().isBlank()
                && (dto.getSenha() == null || dto.getSenha().isBlank()
                || dto.getConfirmarSenha() == null || dto.getConfirmarSenha().isBlank())) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Para alterar a senha, informe nova senha e confirmação.");
        }

        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {

            if (dto.getSenhaAtual() == null || dto.getSenhaAtual().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Para alterar a senha você deve informar a senha atual.");
            }

            if (dto.getConfirmarSenha() == null || dto.getConfirmarSenha().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Confirme a nova senha.");
            }

            if (!dto.getSenha().equals(dto.getConfirmarSenha())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Nova senha e confirmação não são iguais.");
            }

            boolean senhaMatch = passwordEncoder.matches(dto.getSenhaAtual(), usuario.getSenha());

            if (!senhaMatch) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Senha atual incorreta.");
            }

            usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        }

        return usuarioRepository.save(usuario);
    }

    public void remover(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (usuario.getImagemPublicId() != null) {
            try {
                cloudinary.uploader().destroy(usuario.getImagemPublicId(), ObjectUtils.emptyMap());
            } catch (Exception e) {
                System.err.println("Erro ao deletar imagem do usuário: " + e.getMessage());
            }
        }

        if (usuario.getPets() != null) {
            for (Pet pet : usuario.getPets()) {
                if (pet.getImagemPublicId() != null) {
                    try {
                        cloudinary.uploader().destroy(pet.getImagemPublicId(), ObjectUtils.emptyMap());
                    } catch (Exception e) {
                        System.err.println("Erro ao deletar imagem do pet " + pet.getNome() + ": " + e.getMessage());
                    }
                }

                try {
                    cloudinary.api().deleteFolder("pets/" + pet.getId(), ObjectUtils.emptyMap());
                } catch (Exception ignored) {}
            }
        }

        usuarioRepository.delete(usuario);

        try {
            cloudinary.api().deleteFolder("usuarios/" + usuarioId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            System.out.println("Pasta do usuário não pôde ser removida (ainda contém arquivos): " + e.getMessage());
        }

        try {
            cloudinary.api().deleteFolder("pets/" + usuarioId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            System.out.println("Pasta de pets do usuário não está vazia: " + e.getMessage());
        }
    }


    public void solicitarRedefinicaoSenha(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String codigo = String.format("%06d", new Random().nextInt(999999));

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setCodigo(codigo);
        resetToken.setUsuario(usuario);
        resetToken.setExpirationDate(LocalDateTime.now().plusMinutes(10));
        resetToken.setUsed(false);

        resetTokenRepository.save(resetToken);

        String corpoEmail = "Seu código de verificação é: " + codigo + "\n" +
                "Este código expira em 10 minutos.";

        emailService.enviarEmail(
                usuario.getEmail(),
                "Código de verificação para redefinir senha",
                corpoEmail
        );
    }

    public void redefinirSenha(String codigo, String novaSenha) {
        PasswordResetToken resetToken = resetTokenRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Código inválido."));

        if (resetToken.isUsed()) {
            throw new RuntimeException("Código já foi utilizado.");
        }

        if (resetToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Código expirado.");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        resetToken.setUsed(true);
        resetTokenRepository.save(resetToken);
    }


    public Usuario buscarUsuarioPorId(UUID usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public String atualizarImagemUsuario(UUID usuarioId, MultipartFile file) throws IOException {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));


        if (usuario.getImagemPublicId() != null) {
            cloudinary.uploader().destroy(usuario.getImagemPublicId(), ObjectUtils.emptyMap());
        }

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "usuarios/" + usuarioId,
                        "overwrite", true,
                        "resource_type", "image"
                ));

        String imagemUrl = uploadResult.get("secure_url").toString();
        String publicId = uploadResult.get("public_id").toString();

        usuario.setImagemUrl(imagemUrl);
        usuario.setImagemPublicId(publicId);
        usuarioRepository.save(usuario);

        return imagemUrl;
    }



}









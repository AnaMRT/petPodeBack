package app.pet_pode_back.service;

import app.pet_pode_back.dto.UsuarioUpdateDTO;
import app.pet_pode_back.exception.ParametroInvalidoException;
import app.pet_pode_back.exception.RegistroNaoEncontradoException;
import app.pet_pode_back.model.Pet;
import app.pet_pode_back.model.Usuario;
import app.pet_pode_back.repository.UsuarioRepository;
import app.pet_pode_back.security.JwtUtil;
import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

import com.cloudinary.utils.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UsuarioService {
    @Autowired
    private Cloudinary cloudinary;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario editarUsuario(UUID usuarioId, UsuarioUpdateDTO dto) {
        Usuario usuario = buscarUsuarioPorId(usuarioId);

        if (dto.getNome() != null && !dto.getNome().isBlank()) {
            usuario.setNome(dto.getNome());
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            usuario.setEmail(dto.getEmail());
        }

        tratarAtualizacaoDeSenha(usuario, dto);
        return usuarioRepository.save(usuario);
    }

    private void tratarAtualizacaoDeSenha(Usuario usuario, UsuarioUpdateDTO dto) {
        boolean informouSenhaAtual = dto.getSenhaAtual() != null && !dto.getSenhaAtual().isBlank();
        boolean informouNovaSenha = dto.getSenha() != null && !dto.getSenha().isBlank();
        boolean informouConfirmacao = dto.getConfirmarSenha() != null && !dto.getConfirmarSenha().isBlank();

        if (informouSenhaAtual && (!informouNovaSenha || !informouConfirmacao)) {
            throw new ParametroInvalidoException("Para alterar a senha, informe nova senha e confirmação.");
        }

        if (informouNovaSenha) {
            if (!informouSenhaAtual) {
                throw new ParametroInvalidoException("Para alterar a senha, informe também a senha atual.");
            }
            if (!informouConfirmacao) {
                throw new ParametroInvalidoException("Confirme a nova senha.");
            }
            if (!dto.getSenha().equals(dto.getConfirmarSenha())) {
                throw new ParametroInvalidoException("Nova senha e confirmação não coincidem.");
            }
            if (!passwordEncoder.matches(dto.getSenhaAtual(), usuario.getSenha())) {
                throw new ParametroInvalidoException("Senha atual incorreta.");
            }

            usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        }
    }

    public String atualizarImagemUsuario(UUID usuarioId, MultipartFile file) throws IOException {
        Usuario usuario = buscarUsuarioPorId(usuarioId);

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


    private void excluirImagemDoCloud(UUID usuarioId, Usuario usuario) {

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
                } catch (Exception ignored) {
                }
            }
        }

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

    public Map<String, Object> getUsuarioLogado(String token) {

        UUID usuarioId = jwtUtil.extrairUsuarioId(token);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuário não encontrado"));

        Map<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("id", usuario.getId());
        usuarioMap.put("nome", usuario.getNome());
        usuarioMap.put("email", usuario.getEmail());
        usuarioMap.put("imagemUrl", usuario.getImagemUrl());

        return usuarioMap;
    }


    public Usuario buscarUsuarioPorId(UUID usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuário não encontrado"));
    }


    public void remover(UUID usuarioId) {
        Usuario usuario = buscarUsuarioPorId(usuarioId);
        excluirImagemDoCloud(usuarioId, usuario);
        usuarioRepository.delete(usuario);
    }


}









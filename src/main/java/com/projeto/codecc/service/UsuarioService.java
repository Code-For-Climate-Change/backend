package com.projeto.codecc.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.projeto.codecc.model.Usuario;
import com.projeto.codecc.model.UsuarioLogin;
import com.projeto.codecc.repository.UsuarioRepository;
import com.projeto.codecc.security.JwtService;

@Service
public class UsuarioService {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    public Optional<Usuario> cadastrarUsuario(Usuario usuario) {

		if (usuarioRepository.findByUsuario(usuario.getUsuario()).isPresent())
			return Optional.empty();

		if (checarIdade(usuario.getDataNascimento()) < 18)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário menor de idade!", null);
		
		usuario.setSenha(criptografarSenha(usuario.getSenha()));

		return Optional.of(usuarioRepository.save(usuario));
	}

    public Optional<Usuario> atualizarUsuario(Usuario usuario) {

		if (usuarioRepository.findById(usuario.getId()).isPresent()) {

			Optional<Usuario> buscaUsuario = usuarioRepository.findByUsuario(usuario.getUsuario());

			if ((buscaUsuario.isPresent()) && (buscaUsuario.get().getId() != usuario.getId()))
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário já existe!", null);

			if (checarIdade(usuario.getDataNascimento()) < 18)
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário menor de idade!", null);
						
			usuario.setSenha(criptografarSenha(usuario.getSenha()));

			return Optional.ofNullable(usuarioRepository.save(usuario));

		}

		return Optional.empty();

	}

	private int checarIdade(LocalDate dataNascimento) {
		return Period.between(dataNascimento, LocalDate.now()).getYears();
	}

	public Optional<UsuarioLogin> autenticarUsuario(Optional<UsuarioLogin> usuarioLogin) {

		var credenciais = new UsernamePasswordAuthenticationToken(usuarioLogin.get().getUsuario(),
				usuarioLogin.get().getSenha());

		Authentication authentication = authenticationManager.authenticate(credenciais);

		if (authentication.isAuthenticated()) {

			Optional<Usuario> usuario = usuarioRepository.findByUsuario(usuarioLogin.get().getUsuario());

			if (usuario.isPresent()) {

				usuarioLogin.get().setId(usuario.get().getId());
				usuarioLogin.get().setNome(usuario.get().getNome());
				usuarioLogin.get().setFoto(usuario.get().getFoto());
				usuarioLogin.get().setTipoUsuario(usuario.get().getTipoUsuario());
				usuarioLogin.get().setDataNascimento(usuario.get().getDataNascimento());
				usuarioLogin.get().setSenha("");
				usuarioLogin.get().setToken(gerarToken(usuarioLogin.get().getUsuario()));

				return usuarioLogin;
			}
		}

		return Optional.empty();

	}

	private String criptografarSenha(String senha) {

		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		
		return encoder.encode(senha);

	}

	private String gerarToken(String usuario) {
		return "Bearer " + jwtService.generateToken(usuario);
	}

}

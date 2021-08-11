package br.com.generation.blogpessoal.service;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.com.generation.blogpessoal.model.UserLogin;
import br.com.generation.blogpessoal.model.Usuario;
import br.com.generation.blogpessoal.repository.UsuarioRepository;


@Service
public class UsuarioService {

	@Autowired
	public UsuarioRepository usuarioRepository;
	
	// Método para cadastrar usuário
	public Usuario cadastrarUsuario(Usuario usuario) {
		
		// Verificando se o usuário existe no banco de dados
		if(usuarioRepository.findByUsuario(usuario.getUsuario()).isPresent())
			// Se o usuário já existir, séra lançada a exceção indicando que o usuário já existe
			throw new ResponseStatusException(
				// Erro 400 com mensagem 
				HttpStatus.BAD_REQUEST, "O usuário já existe!", null);
		
		// Period.between -> (data de hoje - data nascimento) = idade
		int idade = Period.between(usuario.getDataNascimento(), LocalDate.now()).getYears();
		// Se for menor que 18 anos
		if(idade < 18)
			// Erro 400 pois usuário é menor de 18 anos
			throw new ResponseStatusException(
						HttpStatus.BAD_REQUEST, "O usuário é menor de 18 anos!", null);
		
		// Biblioteca commons codec para criptografar a senha
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		
		// Vai pegar a senha do usuário
		String senhaEncoder = encoder.encode(usuario.getSenha());
		// Encodar a senha
		usuario.setSenha(senhaEncoder);
		
		// optional.of -> indica o que será retornado
		return usuarioRepository.save(usuario);
	}
	
	// Método para atualizar usuário
	public Optional<Usuario> atualizarUsuario(Usuario usuario){
		
		if(usuarioRepository.findById(usuario.getId()).isPresent()) {
			
			Optional<Usuario> buscaUsuario = usuarioRepository.findByUsuario(usuario.getUsuario());
			
			if( buscaUsuario.isPresent() ){

				if(buscaUsuario.get().getId() != usuario.getId())
					throw new ResponseStatusException(
						HttpStatus.BAD_REQUEST, "Usuário já existe!", null);
			}

			int idade = Period.between(usuario.getDataNascimento(), LocalDate.now()).getYears();
			
			if(idade < 18)
				throw new ResponseStatusException(
						HttpStatus.BAD_REQUEST, "Usuário menor de 18 anos", null);
			
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			
			String senhaEncoder = encoder.encode(usuario.getSenha());
			usuario.setSenha(senhaEncoder);
			
			return Optional.of(usuarioRepository.save(usuario));
		
		}else {		
			throw new ResponseStatusException(
					HttpStatus.NOT_FOUND, "Usuário não encontrado!", null);		
		}
		
	}
	
	public Optional<UserLogin> logarUsuario(Optional<UserLogin> usuarioLogin) {
		
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			
		// Procurar entre Usuario e usuarioLogin se o usuário existe dentro do banco
		Optional<Usuario> usuario = usuarioRepository.findByUsuario(usuarioLogin.get().getUsuario());
		
			if(usuario.isPresent()) {
			// matches -> faz a comparação das senhas	
			if(encoder.matches(usuarioLogin.get().getSenha(), usuario.get().getSenha())) {
				// Criar uma string através da concatenção de usuario e senha
				String auth = usuarioLogin.get().getUsuario() + ":" + usuarioLogin.get().getSenha();
				// encodeBase64 -> irá criptografar a concatenação
				byte[] encodeAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
				// Geração do token
				String authHeader = "Basic " + new String(encodeAuth);
			
				// Pega o id que está gravado dentro do banco e salva em usuarioLogin
				usuarioLogin.get().setId(usuario.get().getId());
				usuarioLogin.get().setNome(usuario.get().getNome());
				usuarioLogin.get().setSenha(usuario.get().getSenha());
				usuarioLogin.get().setToken(authHeader);
			
				return usuarioLogin;		
		}
	}
		throw new ResponseStatusException(
			HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos!", null);	
	}
		
}

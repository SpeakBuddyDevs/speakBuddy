package com.speakBuddy.speackBuddy_backend.service;


import com.speakBuddy.speackBuddy_backend.dto.LoginRequestDTO;
import com.speakBuddy.speackBuddy_backend.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider TokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = TokenProvider;
    }


    /**
     * @param request
     * @return El token JWT si se autentica correctamente
     */
    public String login(LoginRequestDTO request){

        //Aqui se autentica que el email y la contraseña son correctos
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        //Tras verificarlo se genera el token y lo devuelve
        String token = tokenProvider.generateToken(authentication);

        return token;
    }

    /**
     * Para el "Logout" se tiene que hacer en el Flutter, hay que borrar
     * el token JWT que tenia guardado en el dispositivo
     */
}

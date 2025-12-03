package com.kbcollection.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterDTO {
    
    @NotBlank(message = "El nombre es obligatorio")
    public String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido (ej: usuario@dominio.com)")
    public String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    public String password;
}
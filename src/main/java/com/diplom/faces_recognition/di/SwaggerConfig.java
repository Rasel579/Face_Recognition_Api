package com.diplom.faces_recognition.di;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title = "Face Recognition API",
                description = "Face Recognition", version = "1.0.0",
                contact = @Contact(
                        name = "Shaykhislamov Ruslan",
                        email = "ruslanshaykh@yandex.ru",
                        url = "http://localhost/"
                )
        )
)
public class SwaggerConfig {
}

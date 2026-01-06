package ps.demo.jpademo.controller;

import brave.Tracer;
import jakarta.websocket.server.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ps.demo.jpademo.config.TraceIdContext;
import ps.demo.jpademo.dto.BaseSuccessResp;
import ps.demo.jpademo.dto.BookDto;
import ps.demo.jpademo.dto.JasyptResponse;

import java.util.List;
import java.util.Scanner;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@Slf4j
@RestController
@RequestMapping("/actuator/api/v1/jasypt")
@Tag(name = "Jasypt Encryption API", description = "API for text encryption and decryption using Jasypt")
public class JasyptController {

    @Value("${jasypt.encryptor.password}")
    private String jasyptEncrytorPass;


    @Operation(
            summary = "Encrypt text",
            description = "Encrypts the provided text using configured Jasypt encryption algorithm",
            method = "GET"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Text encrypted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameter - text cannot be empty"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error - encryption failed"
            )
    })
    @GetMapping("/encrypt")
    ResponseEntity<String> encrypt(
            @Parameter(
                    name = "text",
                    description = "Plain text content to be encrypted",
                    required = true,
                    example = "1234",
                    schema = @Schema(type = "string", minLength = 1)
            )
            @RequestParam String text, @Parameter(
            name = "jasyptPass",
            description = "The jasypt password for encryption, if not provided, will use the default value",
            required = false,
            example = "xxx",
            schema = @Schema(type = "string")
    )
            @RequestParam(required = false) String jasyptPass) {
        if (StringUtils.isEmpty(jasyptPass) && StringUtils.isEmpty(jasyptEncrytorPass)) {
            throw new RuntimeException("jasyptPass cannot be empty");
        }
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        if (StringUtils.isNotEmpty(jasyptPass)) {
            encryptor.setPassword(jasyptPass);
        } else {
            encryptor.setPassword(jasyptEncrytorPass);
        }
        encryptor.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        encryptor.setIvGenerator(new org.jasypt.iv.RandomIvGenerator());
        String result = "ENC(" + encryptor.encrypt(text.trim()) + ")";
        return ResponseEntity.ok(result);
    }


    @Operation(
            summary = "Decrypt encrypted text",
            description = "Decrypts Jasypt-encrypted text back to original plain text",
            operationId = "decryptText"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Text successfully decrypted",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - encrypted text is invalid or malformed"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error during decryption process"
            )
    })
    @GetMapping("/decrypt")
    ResponseEntity<String> decrypt(
            @Parameter(
                    name = "text",
                    description = "Encrypted text to be decrypted (typically starts with ENC())",
                    required = true,
                    example = "ENC(cPc9yxPdt1M7MqRDUDnedgs3QvuZO/D4VSA+EbYb0rlmaGL5YZ7rh9JHa6WbcMRW)",
                    schema = @Schema(type = "string", minLength = 1)
            )
            @RequestParam String text, @Parameter(
            name = "jasyptPass",
            description = "The jasypt password for encryption, if not provided, will use the default value",
            required = true,
            example = "xxx",
            schema = @Schema(type = "string")
    )
            @RequestParam(required = false) String jasyptPass) {
        if (StringUtils.isEmpty(jasyptPass)) {
            throw new RuntimeException("jasyptPass cannot be empty");
        }
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(jasyptPass);
//        if (jasyptPass != null) {
//            encryptor.setPassword(jasyptPass);
//        } else {
//            encryptor.setPassword(jasyptEncrytorPass == null ? " " : jasyptEncrytorPass);
//        }
        encryptor.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        encryptor.setIvGenerator(new org.jasypt.iv.RandomIvGenerator());
        text = text.trim();
        if (text.startsWith("ENC(")) {
            text = StringUtils.removeStart(text, "ENC(");
            text = StringUtils.removeEnd(text, ")");
        }
        String result = encryptor.decrypt(text.trim());

        return ResponseEntity.ok(result);
    }

}

package com.poc.mpt.controller;

import com.poc.mpt.common.GenericApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


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
                            schema = @Schema(implementation = GenericApiResponse.class)
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
    ResponseEntity<GenericApiResponse<String>> encrypt(
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
        return ResponseEntity.ok(GenericApiResponse.success(result));
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
                            schema = @Schema(implementation = GenericApiResponse.class)
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
    ResponseEntity<GenericApiResponse<String>> decrypt(
            @Parameter(
                    name = "text",
                    description = "Encrypted text to be decrypted (typically starts with ENC())",
                    required = true,
                    example = "ENC(AaT1+TQsNzGiZbpyTl3njdCCX3CClSRT13ZBdMTOASwdMn7U7QzJUOioveDjvBAx)",
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

        return ResponseEntity.ok(GenericApiResponse.success(result));
    }

}

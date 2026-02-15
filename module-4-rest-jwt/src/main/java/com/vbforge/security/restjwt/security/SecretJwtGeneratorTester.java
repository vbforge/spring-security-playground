package com.vbforge.security.restjwt.security;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

public class SecretJwtGeneratorTester {
    public static void main(String[] args) {
        System.out.println(
                Encoders.BASE64.encode(Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded())
                //something like this: 23usbJSqMkGck/dftcnlvBrANEPy8IPYisDop+7YHyw=
        );
    }
}

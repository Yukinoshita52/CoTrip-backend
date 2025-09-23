package com.yukino.trip.common.utils;

import com.yukino.trip.common.exception.LeaseException;
import com.yukino.trip.common.result.ResultCodeEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * ClassName: JwtUtil
 * Package: com.yukino.lease.common.utils
 * Description:
 *
 * @Author YukinoshitaYukino
 * @Create 2025/3/25 21:09
 * @Version 1.0
 */
public class JwtUtil {

    private static SecretKey secretKey = Keys.hmacShaKeyFor("dRBjs5EUQHFXFZbCYcCVn42ebRbu3ezU".getBytes());

    public static String createToken(Long userId, String username) {

        String jwt = Jwts.builder()
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .setSubject("LOGIN_USER")
                .claim("userId",userId)
                .claim("username",username)
                .signWith(secretKey,SignatureAlgorithm.HS256)
                .compact();
        return jwt;
    }

    public static Claims parseToken(String token) {
        if(token == null){
            throw new LeaseException(ResultCodeEnum.ADMIN_LOGIN_AUTH);
        }
        try {
            JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(secretKey).build();
            Jws<Claims> claimsJws = jwtParser.parseClaimsJws(token);
            return claimsJws.getBody();
        } catch (ExpiredJwtException e) {
            throw new LeaseException(ResultCodeEnum.TOKEN_EXPIRED);
        } catch (JwtException e) {
            throw new LeaseException(ResultCodeEnum.TOKEN_INVALID);
        }
    }

    public static void main(String[] args) {
        String token = createToken(8L, "13587304241");
        System.out.println("token = " + token);
    }
}

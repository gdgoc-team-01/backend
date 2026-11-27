package com.gdg.backend.domain.post;

import com.gdg.backend.domain.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.HexFormat;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostImage {
    private static final byte[] PNG = HexFormat.of().parseHex("89504e470d0a1a0a");
    private static final byte[] JPEG = HexFormat.of().parseHex("ffd8ff");

    @Id @GeneratedValue
    private Long id;
    @Column(nullable = false, length = 1048576)
    private byte[] data;
    private String mediaType;

    public PostImage(byte[] data) {
        if (startsWith(data, PNG)) {
            mediaType = "image/png";
        } else if (startsWith(data, JPEG)) {
            mediaType = "image/jpeg";
        } else {
            throw new DomainException("PNG 또는 JPEG 이미지를 첨부해주세요.");
        }
        this.data = data;
    }

    private static boolean startsWith(byte[] data, byte[] prefix) {
        return data.length >= prefix.length && Arrays.equals(data, 0, prefix.length, prefix, 0, prefix.length);
    }
}

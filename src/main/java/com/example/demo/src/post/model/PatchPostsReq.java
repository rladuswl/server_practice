package com.example.demo.src.post.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PatchPostsReq {
    private int userIdx;
    private String content; // 인스타그램 수정은 내용만 가능
}

package com.example.demo.src.post;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.config.BaseResponseStatus;
import com.example.demo.src.post.model.GetPostsRes;
import com.example.demo.src.post.model.PatchPostsReq;
import com.example.demo.src.post.model.PostPostsReq;
import com.example.demo.src.post.model.PostPostsRes;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final PostProvider postProvider;
    @Autowired
    private final PostService postService;
    @Autowired
    private final JwtService jwtService;


    public PostController(PostProvider postProvider, PostService postService, JwtService jwtService) {
        this.postProvider = postProvider;
        this.postService = postService;
        this.jwtService = jwtService;
    }

    // 전체 게시물 조회
    @ResponseBody
    @GetMapping("") // http://localhost:9000/posts?userIdx=1
    public BaseResponse<List<GetPostsRes>> getPosts(@RequestParam int userIdx) {
        try{

            List<GetPostsRes> getPostsRes = postProvider.retrievePosts(userIdx);
            return new BaseResponse<>(getPostsRes);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    // 게시물 생성
    @ResponseBody
    @PostMapping("") // http://localhost:9000/posts
    public BaseResponse<PostPostsRes> createPosts(@RequestBody PostPostsReq postPostsReq) {
        try{
            int userIdxByJwt = jwtService.getUserIdx();
            if (postPostsReq.getUserIdx() != userIdxByJwt) {
                return new BaseResponse<>(BaseResponseStatus.INVALID_USER_JWT);
            }

            if (postPostsReq.getContent().length() > 450) {
                return new BaseResponse<>(BaseResponseStatus.POST_POSTS_INVALID_CONTENTS);
            }

            if (postPostsReq.getPostImgUrls().size() < 1) {
                return new BaseResponse<>(BaseResponseStatus.POST_POSTS_EMPTY_IMGURL);
            }

            PostPostsRes postPostsRes = postService.createPosts(postPostsReq.getUserIdx(), postPostsReq);
            return new BaseResponse<>(postPostsRes);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    // 게시물 수정
    @ResponseBody
    @PatchMapping ("/{postIdx}") // http://localhost:9000/posts/6
    public BaseResponse<String> modifyPost(@PathVariable("postIdx") int postIdx, @RequestBody PatchPostsReq patchPostsReq) {
        try{
            if (patchPostsReq.getContent().length() > 450) {
                return new BaseResponse<>(BaseResponseStatus.POST_POSTS_INVALID_CONTENTS);
            }

            postService.modifyPost(patchPostsReq.getUserIdx(), postIdx, patchPostsReq);
            String result = "게시물 수정을 완료하였습니다.";
            return new BaseResponse<>(result);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    // 게시물 삭제 - PATCH
    @ResponseBody
    @PatchMapping ("/{postIdx}/status") // http://localhost:9000/posts/2/status
    public BaseResponse<String> deletePost(@PathVariable("postIdx") int postIdx) {
        try{
            postService.deletePost(postIdx);
            String result = "게시물 삭제를 완료하였습니다.";
            return new BaseResponse<>(result);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    // 게시물 삭제 - DELETE
    @ResponseBody
    @DeleteMapping ("/{postIdx}") // http://localhost:9000/posts/1
    public BaseResponse<String> deletePost2(@PathVariable("postIdx") int postIdx) {
        try{
            postService.deletePost2(postIdx);
            String result = "게시물 삭제를 완료하였습니다.";
            return new BaseResponse<>(result);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

}
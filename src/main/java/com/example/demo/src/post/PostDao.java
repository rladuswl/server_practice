package com.example.demo.src.post;

import com.example.demo.src.post.model.GetPostImgRes;
import com.example.demo.src.post.model.GetPostsRes;
import com.example.demo.src.post.model.PostImgUrlsReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class PostDao {

    private JdbcTemplate jdbcTemplate;
    private List<GetPostImgRes> getPostImgRes;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // 유저가 팔로우 한 유저들의 게시물들 (메인 화면)
    public List<GetPostsRes> selectPosts(int userIdx){
        String selectPostsQuery = "SELECT p.postIdx as postIdx,\n" +
                "                            u.userIdx as userIdx,\n" +
                "                            u.nickName as nickName,\n" +
                "                            u.profileImgUrl as profileImgUrl,\n" +
                "                            p.content as content,\n" +
                "                            IF(postLikeCount is null, 0, postLikeCount) as postLikeCount,\n" +
                "                            IF(commentCount is null, 0, commentCount) as commentCount,\n" +
                "                            case when timestampdiff(second, p.updatedAt, current_timestamp) < 60\n" +
                "                                    then concat(timestampdiff(second, p.updatedAt, current_timestamp), '초 전')\n" +
                "                                when timestampdiff(minute , p.updatedAt, current_timestamp) < 60\n" +
                "                                    then concat(timestampdiff(minute, p.updatedAt, current_timestamp), '분 전')\n" +
                "                                when timestampdiff(hour , p.updatedAt, current_timestamp) < 24\n" +
                "                                    then concat(timestampdiff(hour, p.updatedAt, current_timestamp), '시간 전')\n" +
                "                                when timestampdiff(day , p.updatedAt, current_timestamp) < 365\n" +
                "                                    then concat(timestampdiff(day, p.updatedAt, current_timestamp), '일 전')\n" +
                "                                else timestampdiff(year , p.updatedAt, current_timestamp)\n" +
                "                            end as updatedAt,\n" +
                "                            IF(pl.status = 'ACTIVE', 'Y', 'N') as likeOrNot\n" +
                "                        FROM Post as p\n" +
                "                            join User as u on u.userIdx = p.userIdx\n" +
                "                            left join (select postIdx, userIdx, count(postLikeidx) as postLikeCount from PostLike WHERE status = 'ACTIVE' group by postIdx) plc on plc.postIdx = p.postIdx\n" +
                "                            left join (select postIdx, count(commentIdx) as commentCount from Comment WHERE status = 'ACTIVE' group by postIdx) c on c.postIdx = p.postIdx\n" +
                "                            left join Follow as f on f.followeeIdx = p.userIdx and f.status = 'ACTIVE'\n" +
                "                            left join PostLike as pl on pl.userIdx = f.followerIdx and pl.postIdx = p.postIdx\n" +
                "                        WHERE f.followerIdx = ? and p.status = 'ACTIVE'\n" +
                "                        group by p.postIdx;";
        int selectPostsParam = userIdx;
        // 객체는 queryForObject, 리스트 형태는 query
        return this.jdbcTemplate.query(selectPostsQuery,
                (rs,rowNum) -> new GetPostsRes(
                        rs.getInt("postIdx"),
                        rs.getInt("userIdx"),
                        rs.getString("nickName"),
                        rs.getString("profileImgUrl"),
                        rs.getString("content"),
                        rs.getInt("postLikeCount"),
                        rs.getInt("commentCount"),
                        rs.getString("updatedAt"),
                        rs.getString("likeOrNot"),
                        getPostImgRes = this.jdbcTemplate.query("SELECT pi.postImgUrlIdx,\n"+
                                        "            pi.imgUrl\n" +
                                        "        FROM PostImgUrl as pi\n" +
                                        "            join Post as p on p.postIdx = pi.postIdx\n" +
                                        "        WHERE pi.status = 'ACTIVE' and p.postIdx = ?;\n",
                                (rk, rownum) -> new GetPostImgRes(
                                        rk.getInt("postImgUrlIdx"),
                                        rk.getString("imgUrl")
                                ), rs.getInt("postIdx") // 파라미터 자리 = ? 안에 들어갈 것
                        )
                ), selectPostsParam);
    }

    // 게시물 생성
    public int insertPosts(int userIdx, String content){
        String insertPostQuery = "INSERT INTO Post(userIdx, content) VALUES (?, ?)";
        Object[] insertPostParams = new Object[] {userIdx, content};
        this.jdbcTemplate.update(insertPostQuery,
                insertPostParams);

        // 방금 넣은 post의 idx 값을 다시 클라이언트에게 전달 (return)
        String lastInsertIdxQuery = "SELECT last_insert_id()";
        return this.jdbcTemplate.queryForObject(lastInsertIdxQuery, int.class);
    }

    // 게시물 생성 시, 게시글 이미지도 같이 insert
    public int insertPostImgs(int postIdx, PostImgUrlsReq postImgUrl){
        String insertPostImgsQuery = "INSERT INTO PostImgUrl(postIdx, imgUrl) VALUES (?, ?)";
        Object[] insertPostImgsParams = new Object[] {postIdx, postImgUrl.getImgUrl()};
        this.jdbcTemplate.update(insertPostImgsQuery,
                insertPostImgsParams);

        // 방금 넣은 post의 idx 값을 다시 클라이언트에게 전달 (return)
        String lastInsertIdxQuery = "SELECT last_insert_id()";
        return this.jdbcTemplate.queryForObject(lastInsertIdxQuery, int.class);
    }

    // 게시물 수정
    public int updatePost(int postIdx, String content){
        String updatePostQuery = "UPDATE Post SET content = ? WHERE postIdx = ?";
        Object[] updatePostParams = new Object[] {content, postIdx};
        return this.jdbcTemplate.update(updatePostQuery,
                updatePostParams);
    }

    // 게시물 삭제 - PATCH
    public int deletePost(int postIdx){
        String deletePostQuery = "UPDATE Post SET status = 'INACTIVE' WHERE postIdx = ?";
        // Object[] deletePostParams = new Object[] {postIdx}; // postIdx 하나니까 int 형으로 사용
        int deletePostParams = postIdx;
        return this.jdbcTemplate.update(deletePostQuery,
                deletePostParams);
    }

    // 게시물 삭제 - DELETE
    public int deletePost2(int postIdx){
        String deletePostQuery = "DELETE FROM Post WHERE postIdx = ?";
        // Object[] deletePostParams = new Object[] {postIdx}; // postIdx 하나니까 int 형으로 사용
        int deletePostParams = postIdx;
        return this.jdbcTemplate.update(deletePostQuery,
                deletePostParams);
    }

    // 존재하는 유저인지 확인
    public int checkUserExist(int userIdx){
        String checkUserExistQuery = "select exists(select userIdx from User where userIdx = ?)";
        int checkUserExistParams = userIdx;
        return this.jdbcTemplate.queryForObject(checkUserExistQuery,
                int.class,
                checkUserExistParams);
    }

    // 존재하는 게시물인지 확인
    public int checkPostExist(int postIdx){
        String checkPostExistQuery = "select exists(select postIdx from Post where postIdx = ?)";
        int checkPostExistParams = postIdx;
        return this.jdbcTemplate.queryForObject(checkPostExistQuery,
                int.class,
                checkPostExistParams);
    }
}

package com.example.demo.src.user;


import com.example.demo.src.user.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class UserDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public GetUserInfoRes selectUserInfo(int userIdx){
        String selectUserInfoQuery = "SELECT name, nickName, profileImgUrl, introduce as introduction, website,\n" +
                "       IF (postCount is null, 0, postCount) as postCount,\n" +
                "       IF (followerIdx is null, 0, followerCount) as followerCount,\n" +
                "       IF (followingCount is null, 0, followingCount) as followingCount \n" +
                "FROM User \n" +
                "    left join (SELECT userIdx, COUNT(postIdx) as postCount \n" +
                "                FROM Post\n" +
                "                WHERE status='ACTIVE' \n" +
                "                GROUP BY userIdx) p on p.userIdx = User.userIdx \n" +
                "    left join (SELECT followerIdx, COUNT(followIdx) as followerCount \n" +
                "                FROM Follow\n" +
                "                WHERE status='ACTIVE' \n" +
                "                GROUP BY followerIdx) f1 on f1.followerIdx = User.userIdx \n" +
                "    left join (SELECT followeeIdx, COUNT(followIdx) as followingCount \n" +
                "                FROM Follow\n" +
                "                WHERE status='ACTIVE' \n" +
                "                GROUP BY followeeIdx) f2 on f2.followeeIdx = User.userIdx \n" +
                "WHERE User.userIdx = ? and User.status = 'ACTIVE';";
        int selectUserInfoParam = userIdx;
        // 객체는 queryForObject, 리스트 형태는 query
        return this.jdbcTemplate.queryForObject(selectUserInfoQuery,
                (rs,rowNum) -> new GetUserInfoRes(
                        rs.getString("nickName"),
                        rs.getString("name"),
                        rs.getString("profileImgUrl"),
                        rs.getString("website"),
                        rs.getString("introduction"),
                        rs.getInt("followerCount"),
                        rs.getInt("followingCount"),
                        rs.getInt("postCount")
                ), selectUserInfoParam);
    }

    public List<GetUserPostsRes> selectUserPosts(int userIdx){
        String selectUserPostsQuery = "SELECT p.postIdx as postIdx, pi.imgUrl as postImgUrl\n" +
                "FROM Post as p\n" +
                "    join User as u on u.userIdx = p.userIdx\n" +
                "    join PostImgUrl as pi on pi.postIdx = p.postIdx and pi.status = 'ACTIVE'\n" +
                "WHERE p.status='ACTIVE' and u.userIdx = ?\n" +
                "GROUP BY p.postIdx\n" +
                "HAVING min(pi.postImgUrlIdx)\n" +
                "ORDER BY p.postIdx;";
        int selectUserPostsParam = userIdx;
        // 객체는 queryForObject, 리스트 형태는 query
        return this.jdbcTemplate.query(selectUserPostsQuery,
                (rs,rowNum) -> new GetUserPostsRes(
                        rs.getInt("postIdx"),
                        rs.getString("postImgUrl")
                ), selectUserPostsParam);
    }



    public GetUserRes getUsersByEmail(String email){
        String getUsersByEmailQuery = "select userIdx, name, nickName, email from User where email=?";
        String getUsersByEmailParams = email;
        return this.jdbcTemplate.queryForObject(getUsersByEmailQuery,
                (rs, rowNum) -> new GetUserRes(
                        rs.getInt("userIdx"),
                        rs.getString("name"),
                        rs.getString("nickName"),
                        rs.getString("email")),
                getUsersByEmailParams);
    }


    public GetUserRes getUsersByIdx(int userIdx){
        String getUsersByIdxQuery = "select userIdx, name, nickName, email from User where userIdx=?";
        int getUsersByIdxParams = userIdx;
        return this.jdbcTemplate.queryForObject(getUsersByIdxQuery,
                (rs, rowNum) -> new GetUserRes(
                        rs.getInt("userIdx"),
                        rs.getString("name"),
                        rs.getString("nickName"),
                        rs.getString("email")),
                getUsersByIdxParams);
    }

    public int createUser(PostUserReq postUserReq){
        String createUserQuery = "insert into User (name, nickName, phone, email, password) VALUES (?,?,?,?,?)";
        Object[] createUserParams = new Object[]{postUserReq.getName(), postUserReq.getNickName(),postUserReq.getPhone(), postUserReq.getEmail(), postUserReq.getPassword()};
        this.jdbcTemplate.update(createUserQuery, createUserParams);

        String lastInserIdQuery = "select last_insert_id()";
        return this.jdbcTemplate.queryForObject(lastInserIdQuery,int.class);
    }

    public int checkEmail(String email){
        String checkEmailQuery = "select exists(select email from User where email = ?)";
        String checkEmailParams = email;
        return this.jdbcTemplate.queryForObject(checkEmailQuery,
                int.class,
                checkEmailParams);

    }

    public int checkUserExist(int userIdx){
        String checkUserExistQuery = "select exists(select userIdx from User where userIdx = ?)";
        int checkUserExistParams = userIdx;
        return this.jdbcTemplate.queryForObject(checkUserExistQuery,
                int.class,
                checkUserExistParams);

    }

    public int modifyUserName(PatchUserReq patchUserReq){
        String modifyUserNameQuery = "update User set nickName = ? where userIdx = ? ";
        Object[] modifyUserNameParams = new Object[]{patchUserReq.getNickName(), patchUserReq.getUserIdx()};

        return this.jdbcTemplate.update(modifyUserNameQuery,modifyUserNameParams);
    }





}

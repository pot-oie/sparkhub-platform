package com.pot.sparkhub.service;

public interface CommentLikeService {
    /**
     * [需登录] 点赞一条评论
     */
    void likeComment(Long commentId);

    /**
     * [需登录] 取消点赞一条评论
     */
    void unlikeComment(Long commentId);
}
package com.heima.model.comment.pojos;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class ApCommentRepay implements Serializable {

    private String id;
    private Integer authorId;
    private String authorName;
    private String commentId;
    private String content;
    private Integer likes;
    private Integer longitude;
    private Integer latitude;
    private String address;
    private Date createdTime;
}

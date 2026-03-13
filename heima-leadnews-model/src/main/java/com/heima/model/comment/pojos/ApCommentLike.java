package com.heima.model.comment.pojos;

import lombok.Data;


import java.io.Serializable;

@Data
public class ApCommentLike implements Serializable {

    private String id;
    private Integer authorId;
    private String commentId;
}

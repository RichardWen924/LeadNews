package com.heima.model.comment.pojos;

import lombok.Data;


import java.io.Serializable;
import java.util.Date;

@Data
public class ApComment implements Serializable {

    private String id;
    private Integer authorId;
    private String authorName;
    private Long entryId;
    private Integer channelId;
    private Integer type;
    private String content;
    private String image;
    private Integer likes;
    private Integer reply;
    private Integer flag;
    private Integer longitude;
    private Integer latitude;
    private String address;
    private Integer ord;
    private Date createdTime;
}

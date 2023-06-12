package com.atguigu.common.to;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author:luosheng
 * @Date:2023-05-29 10:11
 * @Description:
 */
@Data
public class SocialUser  implements Serializable {

    private String gists_url;
    private String repos_url;
    private String following_url;
    private String bio;
    private Date created_at;
    private String remark;
    private String login;
    private String type;
    private String subscriptions_url;
    private Date updated_at;
    private long id;
    private int public_repos;
    private String organizations_url;
    private String starred_url;
    private String followers_url;
    private int public_gists;
    private String url;
    private String received_events_url;
    private int watched;
    private int followers;
    private String avatar_url;
    private String events_url;
    private String html_url;
    private int following;
    private String name;
    private int stared;
}

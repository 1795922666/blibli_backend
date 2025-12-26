package com.zspt.blibli.dynamic.mapper.domin;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("bli_dynamic_comment_like")
public class DynamicCommentLike {
      private   Long id;
      private  Long userId;
      private  Long commentId;
      private  Long replyId;
}

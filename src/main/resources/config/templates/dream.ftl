<#assign fcomments="so.nian.backup.freemarker.function.NianStepComments"?new()/>
<#assign fdate="so.nian.backup.freemarker.function.NianDateFromat"?new()/>
<#assign fimage="so.nian.backup.freemarker.function.NianStepImages"?new()/>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${dream.title}</title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <style type="text/css">
        body {
            background-color: #646464;
            TEXT-ALIGN: center;
        }
        body, ul, li {
            margin: 0;
            padding: 0;
        }
        .nian_content {
            margin-top: 30px;
            font-family: '宋体';
            font-size: 11pt;
            white-space: pre-wrap;
            -ms-word-wrap: break-word;
            word-wrap: break-word;
        }
        .nian_userinfo {
            font-family: Consolas, '宋体';
            font-size: 10pt;
            color : #161616;
        }
        .nian_comment {
            font-family: Consolas, '宋体';
            font-size: 10pt;
            color : #161616;
        }
        .nian_booktitle {
            font-family: Consolas, '宋体';
            font-size: 20pt;
            margin-left: 30px;
            font-weight:bold;
        }
        .nian_booknotice {
            font-family: Consolas, '宋体';
            font-size: 12pt;
            margin-left: 30px;
            white-space: pre;
        }
        .grid_wrapper {
            width: 1000px;
            text-align: left;
            margin-left: auto;
            margin-right: auto;
        }
        .picture-grid {
            padding-top: 10px;
            list-style-type: none;
        }
        .picture-grid:after {
            content: ".";
            display: block;
            line-height: 0;
            width: 0;
            height: 0;
            clear: both;
            visibility: hidden;
            overflow: hidden;
        }
        .picture-grid li {
            float: left;
            line-height: 160px;
        }
        .picture-grid li a, img, .picture-grid li a:visited, img:visited {
            display: block;
            border: 1px solid #ddd;
            width: 160px;
            height: 160px;
            text-align: center;
            position: relative;
            z-index: 1;
        }
        .picture-grid li a:hover, img:hover {
            z-index: 2;
        }
        .textblock {
            background-color : #ffffff;
            padding : 10px;
            margin-top: 20px;
            margin-bottom: 20px;
            box-shadow: 1px 1px 1px #888888;
        }
        .head {
            margin-top: 10px;
        }
        .headimage img {
            margin-top: -8px;
            margin-right: 10px;
            width: 60px;
            height: 60px;
            border-radius: 60px;
            float: left;
        }
        .headnick {
            font-family: Consolas, '宋体';
            font-size: 12pt;
            position: relative;
        }
    </style>
</head>

<body>
<div class="grid_wrapper">
    <div class="textblock">
        <div class="head">
            <span class="headimage"><img src="http://img.nian.so/dream/${dream.image}!dream"/></span>
            <span class="headnick">${dream.user}</span>
            <p class="nian_userinfo">进展(${dream.step}), 赞(${dream.like_step}), 听众(${dream.followers}) ${fdate(dream.lastdate)}</p>
        </div>
        <p class="nian_booktitle">${dream.title}</p>
        <p class="nian_booknotice">${dream.content}</p>
    </div>
    <#if steps?size gt 0>
        <#list steps as step>
            <div class="textblock">
                <div class="head">
                    <span class="headimage"><img src="http://img.nian.so/head/${dream.uid}.jpg!dream"/></span>
                    <span class="headnick">${step.user}</span>
                    <p class="nian_userinfo">评论(${step.comments}), 赞(${step.comments}), ${fdate(step.lastdate)}</p>
                </div>
                <#if step.images?size gt 0>
                    <ul class="picture-grid image_tooltip">
                        <#list step.images as image>
                            <li><img src="http://img.nian.so/step/${image.path}" rectinfo="${image.width }x${image.height}" /></li>
                        </#list>
                    </ul>
                </#if>
                <p class="nian_content">${step.content}</p>
                <#assign comments=fcomments(step.sid)/>
                <!--这里需要读取评论-->
                <#if comments??>
                    <#if comments?size gt 0>
                        <hr style="width:400px;margin-left: 0px;text-align: left;"/>
                        <#list comments as comment>
                            <p class="nian_comment">[${fdate(comment.lastdate)}] ${comment.user}: ${comment.content}</p>
                        </#list>
                    </#if>
                </#if>
                <!--这里需要读取点赞的人-->
            </div>
        </#list>
    </#if>
</div>
</body>
<script type="text/javascript" src="jquery-3.3.1.min.js"></script>
<script type="text/javascript">
    $(".image_tooltip img").mouseover(function(e){
        var MAXWIDTH = 600
        var rectinfo = $(this).attr('rectinfo')
        var rects = rectinfo.split("x")
        var width = rects[0]
        var height = rects[1]
        if (width > MAXWIDTH) {
            height = height * MAXWIDTH / width
            width = MAXWIDTH
        }
        var $tooltip = "<div id='image_tooltip'><img src='"+this.src+"'></div>";
        $("body").append($tooltip); //添加到页面中
        $("#image_tooltip").css(
                {
                    "top": e.pageY + "px",
                    "left": e.pageX + "px",
                    "width": width + "px",
                    "height": height + "px",
                    "position": "absolute"
                }).show("fast");    //设置x坐标和y坐标，并显示
    }).mouseout(function(){
        $("#image_tooltip").remove(); //将该div移除
    }).mousemove(function(e){
        $("#image_tooltip").css(
                {
                    "top": e.pageY + "px",
                    "left": e.pageX + "px"
                });
    });
</script>
</html>

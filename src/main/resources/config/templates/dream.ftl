<#assign fcomments="so.nian.backup.freemarker.function.NianStepComments"?new()/>
<#assign fdate="so.nian.backup.freemarker.function.NianDateFromat"?new()/>
<#assign fimage="so.nian.backup.freemarker.function.NianStepImages"?new()/>
<#assign fhtml="so.nian.backup.freemarker.function.NianDealContent"?new()/>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${dream.title}</title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <style type="text/css">
        body {
            background-color: #696969;
            text-align: center;
        }
        body, ul, li {
            margin: 0;
            padding: 0;
        }
        .nian_headblock {
            background-color : #ffffff;
            padding : 20px 40px 20px 40px;
            margin: 20px 0px 20px 0px;
            /*margin-bottom: 20px;*/
            /*box-shadow: 1px 1px 1px #888888;*/
            box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.21), 0 6px 20px 0 rgba(0, 0, 0, 0.20);
            /*border-radius: 20px;*/
            /*transform:rotate(7deg);*/
            /*-webkit-box-reflect:below 0 -webkit-linear-gradient(transparent,transparent 20%,rgba(255,255,255,.3));*/
        }
        .nian_textblock {
            background-color : #ffffff;
            padding : 20px 40px 20px 40px;
            margin: 20px 0px 20px 0px;
            /*border-radius: 20px;*/
            /*box-shadow: 1px 1px 1px #888888;*/
            box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.21), 0 6px 20px 0 rgba(0, 0, 0, 0.20);
            /*transform:rotate(30deg);*/
        }
        .nian_headinfo {
            margin-top: 10px;
        }
        .nian_topheadinfo {
            margin-top: 20px;
        }
        .nian_stepheadimage img {
            margin-top: -8px;
            margin-right: 10px;
            width: 64px;
            height: 64px;
            border-radius: 64px;
            float: left;
        }
        .nian_topbooktitle img {
            margin-left: -2px;
            margin-top: -8px;
            margin-right: 10px;
            width: 80px;
            height: 80px;
            border-radius: 80px;
            float: left;
        }
        .nian_topbookname {
            font-family: Consolas, '宋体', serif;
            /*font-weight: bold;*/
            font-size: 20pt;
            position: relative;
        }
        .nian_dreaminfo {
            font-family: Consolas, '宋体', serif;
            font-size: 11pt;
            margin-left: 10px;
            color : #646464;
            padding-top: 0px;
        }
        .nian_booknotice {
            font-family: Consolas, '宋体', serif;
            font-size: 12pt;
            /*margin-left: 30px;*/
            white-space: pre-wrap;
            padding: 20px 0px 0px 0px;
            line-height : 2;
        }
        .nian_stepheadnick {
            font-family: Consolas, '宋体', serif;
            font-size: 12pt;
            position: relative;
        }
        .nian_stepinfo {
            font-family: Consolas, '宋体', serif;
            font-size: 11pt;
            color : #646464;
        }
        .nian_stepcontent {
            margin: 30px 0px 0px 0px;
            font-family: Consolas, '宋体', serif;
            line-height : 2;
            font-size: 12pt;
            white-space: pre-wrap;
            -ms-word-wrap: break-word;
            word-wrap: break-word;
        }
        .nian_stepcomment {
            margin-left: 0px;
            font-family: Consolas, '宋体', serif;
            font-size: 10pt;
            color : #161616;
        }
        .nian_commentline {
            width: 60%;
            margin-left: 0px;
            text-align: left;
        }
        .grid_wrapper {
            width: 60%;
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
        .layout_left {
            position: fixed;
            background-color: #727272;
            width: 0;
        }
        .layout_right {
            margin-left: 0;
        }
        .button-content-arrow{
            width:20px;
            height: 0%;
            background: white;
            border: 15px solid;
            border-color: #727272 #727272 #727272 #B2DE34 ;
            position: absolute;
            left: 120px;
            top: 10px;
        }
        .button-content{
            width: 100px;
            height: 30px;
            background-color: #B2DE34;
            font-size: small;
            line-height: 30px;
            text-align: center;
            position: absolute;
            top: 10px;
            left: 20px;
        }
        .button-content-bottom {
            width: 10px;
            height: 0%;
            background: white;
            border: 15px solid;
            border-color: #fff #58661C #fff #fff;
            border-top: 0;
            left: -5px;
            position: absolute;
            top: 40px;
        }
    </style>
</head>

<body>
<div class="grid_wrapper">
    <div class="nian_headblock">
        <div class="nian_topheadinfo"><#assign ret=fimage('dream', dream.image)/>
            <span class="nian_topbooktitle"><a href="images/dream/${dream.image}"><img src="images/thumbs/${dream.image}"/></a></span>
            <span class="nian_topbookname">${dream.title}</span>
            <p class="nian_dreaminfo">${dream.user}, 进展(${dream.step}), 赞(${dream.like_step}), 听众(${dream.followers}) ${fdate(dream.lastdate)}</p>
        </div>
        <p class="nian_booknotice">${dream.content}</p>
    </div>
    <#if steps?size gt 0>
    <#list steps as step>
    <div class="nian_textblock">
        <div class="nian_headinfo"><#assign ret=fimage('head', dream.uid + '.jpg')/>
            <span class="nian_stepheadimage"><a href="images/head/${dream.uid}.jpg"><img src="images/thumbs/${dream.uid}.jpg"/></a></span>
            <span class="nian_stepheadnick">${step.user}</span>
            <p class="nian_stepinfo">评论(${step.comments}), 赞(${step.likes}), ${fdate(step.lastdate)}</p>
        </div>
        <#if step.images?size gt 0>
        <ul class="picture-grid image_tooltip">
            <#list step.images as image>
            <#assign ret=fimage('step', image.path)/>
            <li><a href="images/step/${image.path}"><img src="images/thumbs/${image.path}" /></a></li>
            </#list>
        </ul>
        </#if>
        <p class="nian_stepcontent">${step.content}</p>
        <!--这里需要读取评论-->
        <#assign comments=fcomments(step.sid)/>
        <#if comments??>
        <#if comments?size gt 0>
        <hr class="nian_commentline"/>
        <#list comments as comment>
        <p class="nian_stepcomment">[${fdate(comment.lastdate)}] ${comment.user}: ${comment.content}</p>
        </#list>
        </#if>
        </#if>
        <!--这里需要读取点赞的人-->
    </div>
    <#if step?index % 20 == 0><#flush /></#if>
    </#list>
    </#if>
</div>
</body>
</html>

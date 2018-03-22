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
            background-color: #969696;
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
            box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);
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
            font-weight: bold;
            font-size: 20pt;
            position: relative;
        }
        .nian_dreaminfo {
            font-family: Consolas, '宋体', serif;
            font-size: 10pt;
            margin-left: 10px;
            color : #161616;
            padding-top: 5px;
        }
        .nian_booknotice {
            font-family: Consolas, '宋体', serif;
            font-size: 12pt;
            /*margin-left: 30px;*/
            white-space: pre-wrap;
            padding-top: 20px;
            line-height : 2;
        }
        .nian_stepinfo {
            font-family: Consolas, '宋体', serif;
            font-size: 10pt;
            color : #161616;
        }
        .nian_stepcontent {
            margin-top: 30px;
            font-family: Consolas, '宋体', serif;
            line-height : 1.8;
            font-size: 11pt;
            white-space: pre-wrap;
            -ms-word-wrap: break-word;
            word-wrap: break-word;
        }
        .nian_stepcomment {
            font-family: Consolas, '宋体', serif;
            font-size: 10pt;
            color : #161616;
        }
        .grid_wrapper {
            width: 900px;
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
        .nian_textblock {
            background-color : #ffffff;
            padding : 20px 40px 20px 40px;
            margin-top: 20px;
            margin-bottom: 20px;
            /*box-shadow: 1px 1px 1px #888888;*/
            box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);
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
            width: 60px;
            height: 60px;
            border-radius: 60px;
            float: left;
        }
        .nian_stepheadnick {
            font-family: Consolas, '宋体', serif;
            font-size: 12pt;
            position: relative;
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
            <p class="nian_stepinfo">评论(${step.comments}), 赞(${step.comments}), ${fdate(step.lastdate)}</p>
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
        <#assign comments=fcomments(step.sid)/>
        <!--这里需要读取评论-->
        <#if comments??>
            <#if comments?size gt 0>
                <hr style="width:400px;margin-left: 0px;text-align: left;"/>
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

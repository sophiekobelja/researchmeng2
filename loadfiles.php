
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <link rel="stylesheet" href="css/bootstrap.css">
    <script src="js/jquery.js"></script>
    <script src="js/tether.js"></script>
    <script src="js/bootstrap.js"></script>
    <script src="js/d3.js"></script>
    <link rel="stylesheet" type="text/css" href="jsdifflib-master/diffview.css"/>
    <script type="text/javascript" src="jsdifflib-master/diffview.js"></script>
    <script type="text/javascript" src="jsdifflib-master/difflib.js"></script>
    <script type="text/javascript" src="example.js"></script>

</head>
<body>

    <!--    <input type="radio" name="_viewtype" onclick="diffUsingJS();" /> <label for="sidebyside">Side by Side Diff</label>-->
<!--    <button onclick="diffUsingJS();">Side by Side</button>-->
<!--    <button id="sidebyside">Side by Side</button>-->
    <div id="diffoutput"> </div>

    <div>Bug Name <input type="text" value="1504199_DERBY-5283" id="changebug"><button id="changebugname">Set Bug</button></div>
    <div class="loader" style="visibility:hidden"></div>
</body>
</html>


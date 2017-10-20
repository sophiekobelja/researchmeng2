<?php

require_once 'inc/lib.php';

if (!isset($_GET["bug_name"]) || $_GET['bug_name'] == '')
  $_GET['bug_name'] = '157861_DERBY-106';
if (!isset($_GET["graph_num"]) || $_GET['graph_num'] == '')
  $_GET['graph_num'] = 0;

$bugName = $_GET["bug_name"];
$graphNum = $_GET["graph_num"];
//
//$bugName = '421717_DERBY-1459';
//$graphNum = 0;

$pdo = getConnection();
echo $bugName;
$sql = "SELECT graph_data, edit_script FROM edit_script_table_derby WHERE bug_name='$bugName' AND graph_num=$graphNum";
$stmt = $pdo->query($sql);
$result = $stmt->fetch(PDO::FETCH_ASSOC);
$hasQueryResult = ($result === false) ? false : true;
$stmt = null;
$pdo = null;
$references = Array();


if ($hasQueryResult) {
  $graphData = json_decode($result['graph_data'], true);
  $editScript = json_decode($result['edit_script'], true);
  $oldScript = $editScript['oldScripts'];
  $newScript = $editScript['newScripts'];
  $referencesToRange = $editScript['referencesToRange'];
  $output = Array();
  $output['nodes'] = Array();
  $output['links'] = Array();
  $nodeArray = Array();
  $edgeArray = Array();
  $currentAvailableNodeIndex = 0;
  $originalEdges = $graphData['edges'];
  $classname = undefined;
  foreach ($originalEdges as $edge) {
    $src = $edge['src'];
    $srcName = $src['name'];
    $srcNodeType = getNodeType($src['type']);
    if (isset($nodeArray[$srcName])) {
      $srcIndex = $nodeArray[$srcName];
    } else {
      $nodeArray[$srcName] = $currentAvailableNodeIndex;
      $srcIndex = $currentAvailableNodeIndex;
      $currentAvailableNodeIndex++;
      unset($srcScript);
      if ($srcNodeType === 'CM' || $srcNodeType === 'DM') {
        if (isset($oldScript[$srcName]))
          $srcScript = $oldScript[$srcName];
          preg_match('/(?<=\.)([^\.]+)(?=\.[A-Za-z]*\()/', $srcName, $matches, PREG_OFFSET_CAPTURE);
          $classname = $matches[0][0];
          if (isset($referencesToRange[$srcName])) {
              $refScript = $referencesToRange[$srcName];
          }
          //echo "found reference " . $refScript;
      } elseif ($srcNodeType === 'AM') {
        if (isset($newScript[$srcName]))
          $srcScript = $newScript[$srcName];
          if (isset($referencesToRange[$srcName])) {
              $refScript = $referencesToRange[$srcName];
          }
          preg_match('/(?<=\.)([^\.]+)(?=\.[A-Za-z]*\()/', $srcName, $matches, PREG_OFFSET_CAPTURE);
          $classname = $matches[0][0];
      }
        if ($classname == undefined || $classname == "") {
            preg_match("/(?<=\.)([^\.]+)(?=\.<init>)/", $srcName, $matches, PREG_OFFSET_CAPTURE);
            $classname = $matches[0][0];
        }
        if (isset($refScript) && !in_array($references[$srcName])) {
            //echo "src name " . $srcName . " " . $refScript . "<br>";
            $references[$srcName] = $refScript;
        }
        if (isset($srcScript))
        $output['nodes'][] = ["name" => $srcName, "type" => $srcNodeType, "script" => $srcScript, "classname" => $classname, "reference" => $refScript];
      else
        $output['nodes'][] = ["name" => $srcName, "type" => $srcNodeType, "classname" => $classname, "reference" => $refScript];
//      if (count($references) > 0) {
//          foreach ($references as $r) {
//              echo "ref [" . $r[0] . ", " . $r[1];
//              if (count($r) == 4) {
//                  echo ", " . $r[2] . ", " . $r[3];
//              }
//              echo "]";
//          }
//      }
    }

    $dst = $edge['dst'];
    $dstName = $dst['name'];
    $dstNodeType = getNodeType($dst['type']);
    if (isset($nodeArray[$dstName])) {
      $dstIndex = $nodeArray[$dstName];
    } else {
      $nodeArray[$dstName] = $currentAvailableNodeIndex;
      $dstIndex = $currentAvailableNodeIndex;
      $currentAvailableNodeIndex++;
      unset($dstScript);
      if ($dstNodeType === 'CM' || $dstNodeType === 'DM') {
        if (isset($oldScript[$dstName]))
          $dstScript = $oldScript[$dstName];
          if (isset($referencesToRange[$dstName])) {
              $refScript = $referencesToRange[$srcName];
          }          preg_match('/(?<=\.)([^\.]+)(?=\.[A-Za-z]*\()/', $dstName, $matches, PREG_OFFSET_CAPTURE);
          $classname = $matches[0][0];
      } elseif ($dstNodeType === 'AM') {
          if (isset($newScript[$dstName]))
            $dstScript = $newScript[$dstName];
          if (isset($referencesToRange[$srcName])) {
              $refScript = $referencesToRange[$dstName];
          }
          preg_match('/(?<=\.)([^\.]+)(?=\.[A-Za-z]*\()/', $dstName, $matches, PREG_OFFSET_CAPTURE);
          $classname = $matches[0][0];
      }
      else {
          preg_match('/(?<=\/)([^\.\/]+)(?=\.)/', $dstName, $matches, PREG_OFFSET_CAPTURE);
          $classname = $matches[0][0];
          if (isset($referencesToRange[$srcName])) {
              $refScript = $referencesToRange[$dstName];
          }
      }
        if ($classname == undefined || $classname == "") {
            preg_match("/(?<=\.)([^\.]+)(?=\.<init>)/", $srcName, $matches, PREG_OFFSET_CAPTURE);
            $classname = $matches[0][0];
        }
        if (isset($refScript) && !in_array($references[$dstName]))
            $references[$dstName] = $refScript;
        if (isset($dstScript))
            $output['nodes'][] = ["name" => $dstName, "type" => $dstNodeType, "script" => $dstScript, "classname" => $classname, "reference" => $refScript];
        else
            $output['nodes'][] = ["name" => $dstName, "type" => $dstNodeType, "classname" => $classname, "reference" => $refScript];
    }
    $edgeType = getEdgeType($edge['type']);
    $output['links'][] = ['source' => $srcIndex, 'target' => $dstIndex, "type" => $edgeType];
  }

}



?>
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
<div class="container">
  <form action="single.php" method="get">
    <div class="form-group row">
      <label class="col-2 col-form-label text-right" for="bug-name">Bug Name</label>
      <div class="col-4">
        <input type="text" class="form-control" name="bug_name" id="changebug" value="<?=$bugName?>">
      </div>
      <label class="col-2 col-form-label text-right" for="graph-num">Graph Num</label>
      <div class="col-2">
        <input type="text" class="form-control" name="graph_num" id="graph-num" value="<?=$graphNum?>">
      </div>
      <div class="col-2">
        <input type="submit" class="btn btn-primary" id="changebugname" value="Get">
      </div>
    </div>
  </form>
  <?php if ($hasQueryResult) { ?>
  <div class="row">
    <div class="col">
      <svg width="450" height="400">
        <defs>
          <marker id="marker-arrow" markerWidth="12" markerHeight="12" refx="21" refy="4" orient="auto">
            <path d="M 1 1 7 4 1 7 Z" />
          </marker>
        </defs>
      </svg>
    </div>
    <div class="col">
      <p id="script-title"></p>
      <table class="table">
        <thead class="thead-default">
        <tr>
          <th>Type</th>
          <th>Label</th>
          <th>Value</th>
        </tr>
        </thead>
        <tbody id="script-tbody">
        </tbody>
      </table>
    </div>
  </div>
  <?php } else { ?>
  <div class="row">
    <div class="col">
      <div class="alert alert-danger">No query result</div>
    </div>
  </div>
  <?php } ?>

</div>
<?php if ($hasQueryResult) { ?>
<script>
  var nodes = <?=json_encode($output['nodes'])?>;
  var links = <?=json_encode($output['links'])?>;
  var refsToJS = Array();
  <?php foreach(array_keys($references) as $key){ ?>
      var tmpArr = Array();
      <?php foreach($references[$key] as $val) ?>
            tmpArr.push('<?php echo $val?>');
    refsToJS['<?php echo $key;?>'] = tmpArr;
  <?php } ?>
</script>
    <script src="svg-layout.js"></script>
<?php } ?>
<div id="filecontents"></div>
<div id="diffoutput"> </div>
<div class="loader" style="visibility:hidden"></div>
</body>
</html>

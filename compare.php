<?php
require_once 'inc/lib.php';


if (!isset($_GET['bn1']) || $_GET['bn1'] === '')
  $_GET['bn1'] = '421717_DERBY-1459';
if (!isset($_GET['gn1']) || $_GET['gn1'] === '')
  $_GET['gn1'] = 0;
if (!isset($_GET['bn2']) || $_GET['bn2'] === '')
  $_GET['bn2'] = '1520181_DERBY-6324';
if (!isset($_GET['gn2']) || $_GET['gn2'] === '')
  $_GET['gn2'] = 0;
if (!isset($_GET['mapping_num']) || $_GET['mapping_num'] === '')
  $_GET['mapping_num'] = 0;

//$bugName1 = "421717_DERBY-1459";
//$graphNum1 = 0;
//$bugName2 = "1520181_DERBY-6324";
//$graphNum2 = 0;
//$mappingNum = 5;

$bugName1 = $_GET['bn1'];
$graphNum1 = $_GET['gn1'];
$bugName2 = $_GET['bn2'];
$graphNum2 = $_GET['gn2'];
$mappingNum = $_GET['mapping_num'];

$pdo = getConnection();
$sql = "SELECT subgraph1, subgraph2, node_map, edge_map
          FROM edit_script_mapping
          WHERE bn1='$bugName1' AND gn1=$graphNum1
            AND bn2='$bugName2' AND gn2=$graphNum2
            AND mapping_num=$mappingNum";
$stmt = $pdo->query($sql);
$result = $stmt->fetch(PDO::FETCH_ASSOC);
$hasQueryResult = ($result !== false);
$stmt = null;
$pdo = null;

function parseSingleNode(&$nodes, &$nodeSet, $n) {
  $name = $n['name'];
  $type = getNodeType($n['type']);
  if (isset($nodeSet[$name]))
    return $nodeSet[$name];
  else {
    $nodeSet[$name] = count($nodeSet);
    if (isset($n['script']))
      $nodes[] = ['name' => $name, 'type' => $type, 'script' => $n['script']];
    else
      $nodes[] =  ['name' => $name, 'type' => $type];
    return count($nodeSet) - 1;
  }
}

function parseGraph($original) {
  $nodes = Array();
  $links = Array();
  $nodeSet = Array();
  foreach ($original as $edge) {
    $src = $edge['src'];
    $dst = $edge['dst'];
    $edgeType = getEdgeType($edge['type']);

    $srcIndex = parseSingleNode($nodes, $nodeSet, $src);
    $dstIndex = parseSingleNode($nodes, $nodeSet, $dst);

    $links[] = ['source' => $srcIndex, 'target' => $dstIndex, 'type' => $edgeType];
  }
  return ['nodes' => $nodes, 'links' => $links];
}

function repackNodeMap($map) {
  $output = [];
  foreach ($map as $m) {
    $output[$m[0]['name']] = $m[1];
  }
  return $output;
}

function repackGraph($graph, $map) {
  $output = [];
  $newNodes = [];
  foreach ($graph['nodes'] as $node) {
    $name = $node['name'];
    $newName = $map[$name]['name'];
    unset($newScript);
    if (isset($map[$name]['script']))
      $newScript = $map[$name]['script'];
    $node['newName'] = $newName;
    if (isset($newScript))
      $node['newScript'] = $newScript;
    $newNodes[] = $node;
  }
  $output['nodes'] = $newNodes;
  $output['links'] = $graph['links'];
  return $output;
}

if ($hasQueryResult) {
  $originalSubgraph1 = json_decode($result['subgraph1'], true);
  $originalSubgraph2 = json_decode($result['subgraph2'], true);
  $nodeMap = json_decode($result['node_map'], true);
//  $edgeMap = json_decode($result['edge_map'], true);

  $subgraph1 = parseGraph($originalSubgraph1);
//  $subgraph2 = parseGraph($originalSubgraph2);

  $newNodeMap = repackNodeMap($nodeMap);

  $newGraph = repackGraph($subgraph1, $newNodeMap);

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
</head>
<body>
<div class="container">
  <form action="compare.php" method="get">
    <div class="form-group row">
      <div class="col-3">
        <input type="text" class="form-control" name="bn1" value="<?=$bugName1?>">
      </div>
      <div class="col-1">
        <input type="text" class="form-control" name="gn1" value="<?=$graphNum1?>">
      </div>
      <div class="col-3">
        <input type="text" class="form-control" name="bn2" value="<?=$bugName2?>">
      </div>
      <div class="col-1">
        <input type="text" class="form-control" name="gn2" value="<?=$graphNum2?>">
      </div>
      <div class="col-1">
        <input type="text" class="form-control" name="mapping_num" value="<?=$mappingNum?>">
      </div>
      <div class="col-2">
        <input type="submit" class="btn btn-primary" value="Get">
      </div>

    </div>
  </form>
  <?php if ($hasQueryResult) { ?>
  <div class="row">
    <div class="col">
      <svg width="450" height="400" id="common-graph">
        <defs>
          <marker id="marker-arrow" markerWidth="12" markerHeight="12" refx="21" refy="4" orient="auto">
            <path d="M 1 1 7 4 1 7 Z" />
          </marker>
        </defs>
      </svg>
    </div>
  </div>
  <div class="row">
    <div class="col">
      <p id="script-title1"></p>
      <table class="table">
        <thead class="thead-default">
        <tr>
          <th>Type</th>
          <th>Label</th>
          <th>Value</th>
        </tr>
        </thead>
        <tbody id="script-tbody1"></tbody>
      </table>
    </div>
    <div class="col">
      <p id="script-title2"></p>
      <table class="table">
        <thead class="thead-default">
        <tr>
          <th>Type</th>
          <th>Label</th>
          <th>Value</th>
        </tr>
        </thead>
        <tbody id="script-tbody2"></tbody>
      </table>
    </div>
  </div>
  <script>
    var nodes = <?=json_encode($newGraph['nodes'])?>;
    var links = <?=json_encode($newGraph['links'])?>;
  </script>
  <script src="compare-layout.js"></script>
  <?php } else { ?>
  <div class="row">
    <div class="col">
      <div class="alert alert-danger">No query result</div>
    </div>
  </div>
  <?php } ?>
</div>
</body>

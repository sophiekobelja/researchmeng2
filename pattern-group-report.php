<?php
require_once 'inc/lib.php';

// *CM -> AM
$cmAm = [3, 7, 1, 16];

// *CM -> AF
$cmAf = [6, 18];

$maxPatternId = 24;

$restPatterns = [];
for ($i = 1; $i <= $maxPatternId; $i++) {
  if (!in_array($i, $cmAm) && !in_array($i, $cmAf))
    $restPatterns[] = $i;
}

$patterns = array_merge($cmAm, $cmAf, $restPatterns);

$pdo = getConnection();

?>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
  <link rel="stylesheet" href="css/bootstrap.css">
  <style>
    .pattern-img {
      max-height: 300px;
      max-width: 300px;
    }
  </style>
</head>
<body>
<table class="table">
  <thead>
  <tr>
    <th>Pattern</th>
    <th>#(Commits)</th>
    <th>#(Subgraphs)</th>
  </tr>
  </thead>
  <tbody>
  <?php
  foreach ($patterns as $pid) {
    $stmt = $pdo->query("SELECT commit_num, subgraph_num FROM script_stat_pattern_group_num WHERE pattern_id=$pid");
    echo $stmt;
    $result = $stmt->fetch(PDO::FETCH_ASSOC);
    $commitNum = $result['commit_num'];
    $subgraphNum = $result['subgraph_num'];

  ?>
  <tr>
    <td><img class="pattern-img" src="patternPics/<?=$pid?>.png"></td>
    <td><?=$commitNum?></td>
    <td><?=$subgraphNum?></td>
  </tr>
  <?php } ?>
  </tbody>
</table>
</body>

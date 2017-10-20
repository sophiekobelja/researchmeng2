<?php
require_once 'inc/lib.php';

$pdo = getConnection();
$stmt = $pdo->query('SELECT max(pattern_id) FROM largest_match_pattern');
$result = $stmt->fetch(PDO::FETCH_ASSOC);
$maxPatternId = $result['max(pattern_id)'];
$stmt = $pdo->query('SELECT count(*) FROM largest_match_with_pattern_id');
$result = $stmt->fetch(PDO::FETCH_ASSOC);
$totalSubgraphNum = $result['count(*)'];
$stmt = $pdo->query('SELECT count(DISTINCT bug_name) FROM largest_match_with_pattern_id');
$result = $stmt->fetch(PDO::FETCH_ASSOC);
$totalCommitNum = $result['count(DISTINCT bug_name)'];
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
    <th>Pattern ID</th>
    <th>Shape</th>
    <th>#(Commits)</th>
    <th>#(Subgraphs)</th>
  </tr>
  </thead>
  <tbody>
  <tr>
    <td>Total</td>
    <td></td>
    <td><?=$totalCommitNum?></td>
    <td><?=$totalSubgraphNum?></td>
  </tr>
  <?php for ($patternId = 1; $patternId <= $maxPatternId; $patternId++) {
    echo '<tr>';
    echo "<td>$patternId</td>";
    echo "<td><img class=\"pattern-img\" src=\"largestPatternPic/$patternId.png\"></td>";
    $stmt = $pdo->query("SELECT count(DISTINCT bug_name) FROM largest_match_with_pattern_id WHERE pattern_id=$patternId");
    $result = $stmt->fetch(PDO::FETCH_NUM);
    $commitNum = $result[0];
    echo "<td>$commitNum</td>";
    $stmt = $pdo->query("SELECT count(*) FROM largest_match_with_pattern_id WHERE pattern_id=$patternId");
    $result = $stmt->fetch(PDO::FETCH_NUM);
    $subgraphNum = $result[0];
    echo "<td>$subgraphNum</td>";
    echo '</tr>';
  } ?>
  </tbody>
</table>
</body>

<?php
require_once 'inc/lib.php';
$pdo = getConnection();
$stmt = $pdo->query('SELECT max(pattern_id) FROM largest_match_pattern');
$result = $stmt->fetch(PDO::FETCH_ASSOC);
$maxPatternId = $result['max(pattern_id)'];


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
    <th>ID</th>
    <th>Shape</th>
    <th>Detail</th>
  </tr>
  </thead>
  <tbody>
<?php
for ($patternId = 1; $patternId <= $maxPatternId; $patternId++) {
  echo '<tr>';
  echo "<td>$patternId</td>";
  echo "<td><img class=\"pattern-img\" src=\"largestPatternPic/$patternId.png\"></td>";
  echo "<td>";

  $stmt = $pdo->query("SELECT * FROM largest_match_with_pattern_id WHERE pattern_id=$patternId");
  while ($result = $stmt->fetch(PDO::FETCH_ASSOC)) {
    $bugName = $result['bug_name'];
    echo "<h5>$bugName</h5>";
    $nodeMapJson = $result['node_map'];
    $nodeMap = json_decode($nodeMapJson, true);
    foreach ($nodeMap as $key => $value) {
      $sig = htmlspecialchars($value);
      echo "<p><strong>$key</strong>: $sig</p>";
    }
  }




  echo '</td>';

  echo '</tr>';
}

?>
  </tbody>
</table>
</body>

<?php
require_once 'inc/lib.php';
$pdo = getConnection();
$sql = "SELECT MAX(pattern_id) FROM script_stat_grouped_pattern";
$stmt = $pdo->query($sql);
$result = $stmt->fetch(PDO::FETCH_ASSOC);
$maxPatternId = $result['MAX(pattern_id)'];
$stmt = null;

$allPatterns = [];
for ($patternId = 1; $patternId < $maxPatternId; $patternId++) {
  $sql2 = "SELECT bn1,gn1,bn2,gn2,mapping_num,common_script FROM script_stat_report WHERE pattern_id=$patternId";
  $stmt = $pdo->query($sql2);
  $onePattern = [];
  while ($result = $stmt->fetch(PDO::FETCH_ASSOC)) {
    $bn1 = $result['bn1'];
    $gn1 = $result['gn1'];
    $bn2 = $result['bn2'];
    $gn2 = $result['gn2'];
    $mappingNum = $result['mapping_num'];
    $commonScriptJson = $result['common_script'];
    $commonScript = json_decode($commonScriptJson, true);

    $onePattern[] = ['patternId'=>$patternId, 'bn1'=>$bn1, 'gn1'=>$gn1, 'bn2'=>$bn2, 'gn2'=>$gn2, 'mappingNum'=>$mappingNum, 'commonScript'=>$commonScript];
  }
  $allPatterns[] = $onePattern;
}




?>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
  <link rel="stylesheet" href="css/bootstrap.css">
</head>
<body>
<table class="table">
  <thead>
  <tr>
    <th>Pattern</th>
    <th>bn1,gn1,bn2,gn2,mappingNum</th>
    <th>Common Edit Script</th>
  </tr>
  </thead>
  <tbody>
  <?php
  foreach ($allPatterns as $p) {
    $numInPattern = count($p);
    for ($i = 0; $i < $numInPattern; $i++) {
      $data = $p[$i];
      $patternId = $data['patternId'];
      $bn1 = $data['bn1'];
      $gn1 = $data['gn1'];
      $bn2 = $data['bn2'];
      $gn2 = $data['gn2'];
      $mappingNum = $data['mappingNum'];
      $commonScript = $data['commonScript'];
  ?>
  <tr>
  <?php if ($i === 0) { ?>
    <td rowspan="<?=$numInPattern?>"><img src="patternPics/<?=$patternId?>.png" width="300"></td>
  <?php } ?>
    <td><p><?=$bn1?></p><p><?=$gn1?></p><p><?=$bn2?></p><p><?=$gn2?></p><p><?=$mappingNum?></p></td>
    <td>
  <?php foreach ($commonScript as $nodeLcs) { ?>
      <h5><?=$nodeLcs['shapeNode']?></h5>
      <p>left: <?=htmlspecialchars($nodeLcs['left'])?></p>
      <p>right: <?=htmlspecialchars($nodeLcs['right'])?></p>
  <?php
    if (isset($nodeLcs['commonScript'])) {
      $lcs = $nodeLcs['commonScript'];
  ?>
      <h6>lcs:</h6>
      <ul>
  <?php
      foreach ($lcs as $edit) {
  ?>
        <li>type: <?=$edit['type']?>, label: <?=$edit['label']?></li>
  <?php
      }
  ?>
      </ul>
  <?php
    }
  ?>
  <?php } ?>
    </td>
  </tr>
  <?php
    }
  }
  ?>
  </tbody>
</table>
</body>
</html>

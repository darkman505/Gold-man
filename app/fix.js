const fs = require('fs');
const file = 'app/src/main/java/com/example/ui/screens/SellerScreen.kt';
let content = fs.readFileSync(file, 'utf8');

content = content.replace(/KeyboardType\.Number/g, 'KeyboardType.Decimal');
content = content.replace(/label = "عدد",\n *keyboardOptions = KeyboardOptions\(keyboardType = KeyboardType\.Decimal\)/g, 'label = "عدد",\n                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)');

content = content.replace(/modifier = Modifier(.*)\.height\([0-9]+\.dp\)/g, (match, p1) => {
    if (p1 === '') return match;
    return `modifier = Modifier${p1}`;
});

fs.writeFileSync(file, content);
console.log('Fixed file');

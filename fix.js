const fs = require('fs');
const file = 'app/src/main/java/com/example/ui/screens/SellerScreen.kt';
let content = fs.readFileSync(file, 'utf8');

// Replace all KeyboardType.Number with KeyboardType.Decimal
content = content.replace(/KeyboardType\.Number/g, 'KeyboardType.Decimal');

// Revert qty back to Number (since we know qty needs Number, maybe not strictly needed but good)
content = content.replace(/label = "عدد",\n *keyboardOptions = KeyboardOptions\(keyboardType = KeyboardType\.Decimal\)/g, 'label = "عدد",\n                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)');

// Remove height modifiers from GoldTextFields. We know they usually are in modifiers like:
// modifier = Modifier.fillMaxWidth().height(45.dp)
// modifier = Modifier.weight(1.2f).height(55.dp)
// Let's replace .height(XX.dp) if it's on a line inside a GoldTextField?
// Or simpler, just regex replace `.height\([0-9]+\.dp\)` for specific occurrences, 
// but Spacer also uses it. So let's match `modifier = Modifier\.[^S]*\.height\([0-9]+\.dp\)`
// No, Spacer usually is `modifier = Modifier.height(...)`. TextFields are `modifier = Modifier.width(...).height(...)` or `weight(...).height(...)`
content = content.replace(/modifier = Modifier(.*)\.height\([0-9]+\.dp\)/g, (match, p1) => {
    // If it's a Spacer, don't replace
    if (p1 === '') return match;
    return `modifier = Modifier${p1}`;
});

fs.writeFileSync(file, content);
console.log('Fixed file');

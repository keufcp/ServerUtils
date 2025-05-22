const fs = require('fs');
const path = require('path');

const dir = 'src/main/resources/assets/serverutils/lang';
const files = fs.readdirSync(dir).filter(file => file.endsWith('.json'));

const keys = files.map(file => {
  const content = JSON.parse(fs.readFileSync(path.join(dir, file), 'utf8'));
  return Object.keys(content).sort();
});

const reference = keys[0];
if (!keys.every(k => JSON.stringify(k) === JSON.stringify(reference))) {
  console.error('Translation files have inconsistent keys');
  process.exit(1);
} else {
  console.log('All translation files are consistent');
}

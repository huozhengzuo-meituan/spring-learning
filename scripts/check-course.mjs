import { readdir, readFile, access } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = fileURLToPath(new URL('..', import.meta.url));
const files = ['index.html', ...(await readdir(path.join(root, 'lessons'))).filter(f=>f.endsWith('.html')).map(f=>`lessons/${f}`), ...(await readdir(path.join(root, 'reference'))).filter(f=>f.endsWith('.html')).map(f=>`reference/${f}`)];
const issues = [];
let checkedLinks = 0;
for (const file of files) {
  const html = await readFile(path.join(root, file), 'utf8');
  if (/(?:href|src)=\\"/.test(html)) issues.push(`${file}: escaped HTML attribute`);
  if (!html.startsWith('<!doctype html>') || !html.includes('lang="zh-CN"') || !html.includes('assets/course.css')) issues.push(`${file}: missing document structure`);
  for (const match of html.matchAll(/(?:href|src)="([^"]+)"/g)) {
    const href = match[1].replaceAll('&amp;', '&');
    if (/^(https?:|mailto:|data:)/.test(href)) continue;
    const [target, anchor] = href.split('#');
    const destination = target ? path.resolve(root, path.dirname(file), decodeURIComponent(target)) : path.resolve(root, file);
    try {
      await access(destination);
      if (anchor && destination.endsWith('.html')) {
        const content = await readFile(destination, 'utf8');
        if (!content.includes(`id="${anchor}"`) && !content.includes(`id='${anchor}'`)) issues.push(`${file}: missing anchor ${href}`);
      }
      checkedLinks++;
    } catch { issues.push(`${file}: missing target ${href}`); }
  }
}
const lessons = (await Promise.all(['part-1', 'part-2'].map(async name=>JSON.parse(await readFile(path.join(root, 'course', `${name}.json`),'utf8'))))).flat();
for (const lesson of lessons) {
  const prose = lesson.sections.map(s=>s.html).join('').replace(/<[^>]*>/g, '');
  const chinese = (prose.match(/[\u3400-\u9fff]/g) || []).length;
  if (chinese < 600) issues.push(`Lesson ${lesson.id}: under 600 Chinese characters (${chinese})`);
  if (new Set(lesson.quiz.options.map(s=>[...s].length)).size !== 1) issues.push(`Lesson ${lesson.id}: quiz options have unequal lengths`);
  if (!lesson.exercise.steps.length || !lesson.exercise.expected || !lesson.exercise.solution) issues.push(`Lesson ${lesson.id}: incomplete feedback loop`);
}
if (files.length !== 55) issues.push(`Expected 55 HTML files, got ${files.length}`);
if (issues.length) { console.error(issues.join('\n')); process.exitCode = 1; }
else console.log(`PASS: ${files.length} HTML documents, ${lessons.length} substantive lessons, ${checkedLinks} local links/anchors, equal-length quizzes.`);

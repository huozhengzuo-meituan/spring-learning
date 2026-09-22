/* Shared, dependency-free learning controls. A checked lesson records practice, never mastery. */
(() => {
  'use strict';
  const KEY = 'spring-learning.practice.v1.';
  let progress = {};
  let canStore = true;
  const pendingChanges = new Map();
  function readProgress() {
    const latest = {};
    try {
      for (let id = 1; id <= 48; id++) {
        const raw = localStorage.getItem(KEY + id);
        if (!raw) continue;
        try {
          const item = JSON.parse(raw);
          if (item && typeof item.date === 'string' && Number.isFinite(new Date(item.date).getTime())) latest[id] = item;
        } catch { /* One corrupt entry does not invalidate the other lessons. */ }
      }
      for (const [id, item] of pendingChanges) {
        if (item) latest[id] = item;
        else delete latest[id];
      }
      progress = latest;
      canStore = pendingChanges.size === 0;
    } catch { canStore = false; }
  }
  readProgress();
  const status = document.querySelector('[data-status]');
  const announce = text => { if (status) status.textContent = text; };
  // Independent keys keep edits in other lesson tabs from being overwritten.
  const save = () => {
    pendingChanges.set(lessonId, progress[lessonId] || null);
    try {
      if (progress[lessonId]) localStorage.setItem(KEY + lessonId, JSON.stringify(progress[lessonId]));
      else localStorage.removeItem(KEY + lessonId);
      pendingChanges.delete(lessonId);
      canStore = true;
    } catch { canStore = false; }
  };
  const lessonId = document.body.dataset.lessonId;
  const practiceButton = document.querySelector('[data-practiced]');
  function updatePractice() {
    if (!practiceButton) return;
    const done = Boolean(progress[lessonId]);
    practiceButton.textContent = done ? '撤销本课练习记录' : '我已完成本课练习';
    practiceButton.setAttribute('aria-pressed', String(done));
    const review = document.querySelector('[data-review-date]');
    if (review) {
      const date = new Date(progress[lessonId]?.date);
      review.textContent = done && Number.isFinite(date.getTime())
        ? `练习记录：${date.toLocaleDateString('zh-CN')}。建议在 1、3、7、14 天后不看答案复述，并重做一个变式。`
        : '完成练习后记录日期；明天、第 3 天、第 7 天与第 14 天进行间隔回忆。';
    }
  }
  practiceButton?.addEventListener('click', () => {
    readProgress();
    if (progress[lessonId]) delete progress[lessonId];
    else progress[lessonId] = { date: new Date().toISOString() };
    save(); updatePractice();
    announce(canStore ? '练习记录已保存到当前浏览器。' : '浏览器存储不可用；本次记录仅在当前页面有效，请导出保存。');
  });
  updatePractice();
  document.querySelectorAll('.quiz').forEach(form => {
    form.addEventListener('submit', event => {
      event.preventDefault();
      const selected = form.querySelector('input:checked');
      const feedback = form.querySelector('.feedback');
      if (!selected) { feedback.textContent = '先选择一个答案，再检查推理。'; return; }
      const correct = selected.value === form.dataset.answer;
      feedback.textContent = `${correct ? '判断正确。' : '再想一步。'} ${form.dataset.explanation}`;
    });
  });
  const cards = [...document.querySelectorAll('[data-card]')];
  const search = document.querySelector('[data-search]');
  const filter = document.querySelector('[data-filter]');
  function updateIndex() {
    const query = (search?.value || '').trim().toLocaleLowerCase();
    let visible = 0;
    for (const card of cards) {
      const done = Boolean(progress[card.dataset.card]);
      card.querySelector('[data-completion]').textContent = done ? '已练习' : '待练习';
      const match = card.textContent.toLocaleLowerCase().includes(query) && (filter?.value !== 'todo' || !done) && (filter?.value !== 'done' || done);
      card.hidden = !match;
      if (match) visible++;
    }
    document.querySelectorAll('[data-phase]').forEach(phase => { phase.hidden = ![...phase.querySelectorAll('[data-card]')].some(card => !card.hidden); });
    const count = document.querySelector('[data-result-count]');
    if (count) count.textContent = `显示 ${visible} / ${cards.length} 课`;
    const doneCount = cards.filter(card => Boolean(progress[card.dataset.card])).length;
    const total = document.querySelector('[data-progress-count]');
    if (total) total.textContent = `${doneCount} / ${cards.length} 课已练习`;
    const fill = document.querySelector('[data-progress-fill]');
    if (fill) fill.style.width = `${cards.length ? doneCount / cards.length * 100 : 0}%`;
  }
  search?.addEventListener('input', updateIndex); filter?.addEventListener('change', updateIndex); updateIndex();
  function refresh() { readProgress(); updatePractice(); updateIndex(); }
  window.addEventListener('pageshow', refresh);
  window.addEventListener('storage', event => { if (event.key === null || event.key.startsWith(KEY)) refresh(); });
  document.querySelectorAll('[data-export]').forEach(button => button.addEventListener('click', () => {
    readProgress();
    const url = URL.createObjectURL(new Blob([JSON.stringify({ course: 'spring-learning', exportedAt: new Date().toISOString(), practice: progress }, null, 2)], { type: 'application/json' }));
    const link = document.createElement('a'); link.href = url; link.download = 'spring-learning-progress.json'; link.click();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
    announce('已导出自报练习记录；可在后续对话中结合代码与答案确认学习证据。');
  }));
  if (!canStore) announce('本地存储不可用。课程和测验仍可阅读使用；练习记录请导出保存。');
})();

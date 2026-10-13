/* 公共 JS：API 封装 + 用户状态 */
const API = {
    async request(url, options = {}) {
        const resp = await fetch(url, {
            headers: { 'Content-Type': 'application/json' },
            ...options
        });
        const data = await resp.json();
        return data;
    },
    get(url) { return this.request(url); },
    post(url, body) { return this.request(url, { method: 'POST', body: JSON.stringify(body) }); }
};

function showToast(msg, duration = 2200) {
    let t = document.getElementById('toast');
    if (!t) {
        t = document.createElement('div');
        t.id = 'toast';
        t.className = 'toast';
        document.body.appendChild(t);
    }
    t.textContent = msg;
    t.classList.add('show');
    setTimeout(() => t.classList.remove('show'), duration);
}

/* 获取当前登录用户，未登录跳登录页 */
async function getMe(redirect = true) {
    const r = await API.get('/api/auth/me');
    if (r.code === 0) return r.data;
    if (redirect) location.href = '/login.html';
    return null;
}

/* 渲染顶部导航（页面加载时调用） */
async function initNav(activePage) {
    const user = await getMe(true);
    if (!user) return;
    const navUser = document.querySelector('.nav-user');
    if (navUser) {
        const adminLink = user.role === 'ADMIN'
            ? '<a href="/admin.html">管理后台</a>' : '';
        navUser.innerHTML = `<span class="uname">${user.nickname || user.username}</span>` +
            adminLink + '<a href="#" onclick="logout()">退出</a>';
    }
    // 高亮当前导航
    document.querySelectorAll('.nav-links a').forEach(a => {
        if (a.getAttribute('href') === `/${activePage}.html`) a.classList.add('active');
    });
    return user;
}

async function logout() {
    await API.post('/api/auth/logout', {});
    location.href = '/login.html';
}

/* 格式化金额 */
function fmtMoney(v) {
    return '¥' + (Number(v || 0).toFixed(2));
}

// validate.js — beautiful banking-grade validations
(() => {
  const $$ = (s, r = document) => Array.from(r.querySelectorAll(s));
  const $  = (s, r = document) => r.querySelector(s);

  /* ------------ Utils ------------ */
  const digits = (v) => v.replace(/\D+/g, "");
  const clamp2 = (n) => (isNaN(n) ? NaN : Math.round(n * 100) / 100);
  const INR = (n) =>
    isNaN(n) ? "" : new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 2 }).format(n);
  const isEmail = (v) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v.trim());
  const isPhoneIN = (v) => /^[6-9]\d{9}$/.test(digits(v));
  const hasU = (v) => /[A-Z]/.test(v);
  const hasL = (v) => /[a-z]/.test(v);
  const hasD = (v) => /\d/.test(v);
  const hasS = (v) => /[^A-Za-z0-9]/.test(v);
  const on = (el, ev, fn) => el && el.addEventListener(ev, fn);

  /* ------------ Toasts ------------ */
  function ensureToastHost() {
    let host = $("#toasts");
    if (!host) {
      host = document.createElement("div");
      host.id = "toasts";
      document.body.appendChild(host);
    }
    return host;
  }
  function toast(msg, type = "info", ms = 3000) {
    const host = ensureToastHost();
    const t = document.createElement("div");
    t.className = `toast ${type}`;
    t.innerHTML = `<span class="icon">${type === "success" ? "✅" : type === "error" ? "❌" : "ℹ️"}</span><span>${msg}</span>`;
    host.appendChild(t);
    requestAnimationFrame(() => t.classList.add("show"));
    setTimeout(() => {
      t.classList.remove("show"); setTimeout(() => t.remove(), 300);
    }, ms);
  }

  /* ------------ Field state ------------ */
  function setState(input, ok, msg = "") {
    const field = input.closest(".field") || input.parentElement;
    field.classList.remove("valid", "invalid", "shake");
    field.classList.add(ok ? "valid" : "invalid");
    let hint = field.querySelector(".hint");
    if (!hint) { hint = document.createElement("div"); hint.className = "hint"; field.appendChild(hint); }
    hint.textContent = msg;
    hint.setAttribute("aria-live", "polite");
    if (!ok) { field.classList.add("shake"); setTimeout(() => field.classList.remove("shake"), 350); }
  }

  /* ------------ Buttons (loading) ------------ */
  function setLoading(btn, on = true) {
    if (!btn) return;
    if (on) {
      btn.dataset.submitting = "1"; btn.disabled = true; btn.classList.add("loading");
      btn.dataset.originalText = btn.dataset.originalText || btn.innerHTML;
      btn.innerHTML = `<span class="spinner"></span>${btn.dataset.loadingText || "Processing…"}`;
    } else {
      btn.dataset.submitting = ""; btn.disabled = false; btn.classList.remove("loading");
      if (btn.dataset.originalText) btn.innerHTML = btn.dataset.originalText;
    }
  }

  /* ------------ Amount UX ------------ */
  function attachAmount(input, otpPrompt) {
    input.setAttribute("inputmode", "decimal");
    input.setAttribute("min", "1"); input.setAttribute("step", "0.01");
    const field = input.closest(".field");

    let preview = field.querySelector(".amt-preview");
    if (!preview) { preview = document.createElement("div"); preview.className = "amt-preview"; field.appendChild(preview); }

    const update = () => {
      let v = input.value.replace(/[^\d.]/g, "");
      const i = v.indexOf(".");
      if (i !== -1) v = v.slice(0, i + 1) + v.slice(i + 1).replace(/\./g, "");
      const parts = v.split(".");
      if (parts[1] && parts[1].length > 2) v = parts[0] + "." + parts[1].slice(0, 2);
      input.value = v;

      const n = clamp2(parseFloat(v));
      const ok = !isNaN(n) && n >= 1 && n <= 1000000;
      preview.textContent = !isNaN(n) ? `Preview: ${INR(n)}` : "";

      if (!ok) setState(input, false, "Enter a valid amount (₹1 – ₹10,00,000).");
      else {
        setState(input, true, n > 10000 ? "OTP will be required for this amount." : "");
        if (otpPrompt) otpPrompt.hidden = !(n > 10000);
        if (otpPrompt) otpPrompt.querySelector(".otp-amt").textContent = INR(n);
      }
      return ok;
    };
    on(input, "input", update); on(input, "blur", update);
    return update;
  }

  /* ------------ OTP 6-box widget ------------ */
  function attachOtpGrid(form) {
    const hidden = form.querySelector('input[name="otp"]');
    const grid = form.querySelector(".otp-grid");
    if (!hidden || !grid) return () => true;

    // Build boxes + dots if missing
    if (!grid.querySelector(".otp-box")) {
      for (let i = 0; i < 6; i++) {
        const b = document.createElement("input");
        b.className = "otp-box"; b.maxLength = 1; b.inputMode = "numeric";
        grid.appendChild(b);
      }
      const dots = document.createElement("div");
      dots.className = "otp-dots";
      dots.innerHTML = "<span></span><span></span><span></span><span></span><span></span><span></span>";
      grid.after(dots);
    }

    hidden.type = "hidden";
    const boxes = $$(".otp-box", grid);
    const dots = $$(".otp-dots span", form);

    function syncHidden() {
      const val = boxes.map(b => b.value).join("");
      hidden.value = val;
      dots.forEach((d, idx) => d.classList.toggle("f", idx < val.length));
      return val.length === 6;
    }

    boxes.forEach((box, idx) => {
      on(box, "input", () => {
        box.value = digits(box.value).slice(0, 1);
        if (box.value && idx < 5) boxes[idx + 1].focus();
        syncHidden();
      });
      on(box, "keydown", (e) => {
        if (e.key === "Backspace" && !box.value && idx > 0) {
          boxes[idx - 1].focus(); boxes[idx - 1].value = ""; syncHidden();
        }
        if (!/^\d$/.test(e.key) && !["Backspace", "Tab", "ArrowLeft", "ArrowRight"].includes(e.key)) e.preventDefault();
      });
      on(box, "paste", (e) => {
        const text = digits((e.clipboardData || window.clipboardData).getData("text")).slice(0, 6);
        e.preventDefault();
        boxes.forEach((b, i) => b.value = text[i] || "");
        syncHidden();
        (text.length >= 6 ? boxes[5] : boxes[text.length] || boxes[0]).focus();
      });
    });

    // focus first
    setTimeout(() => boxes[0].focus(), 0);
    return () => syncHidden();
  }

  /* ------------ Password strength + rules ------------ */
  function attachPassword(input) {
    const field = input.closest(".field");
    let meter = field.querySelector(".strengthbar");
    let rules = field.querySelector(".pw-rules");
    if (!meter) {
      meter = document.createElement("div");
      meter.className = "strengthbar";
      meter.innerHTML = `<div class="bar"></div>`;
      field.appendChild(meter);
    }
    if (!rules) {
      rules = document.createElement("ul");
      rules.className = "pw-rules";
      rules.innerHTML = `
        <li data-r="len">8+ characters</li>
        <li data-r="u">Uppercase</li>
        <li data-r="l">Lowercase</li>
        <li data-r="d">Number</li>
        <li data-r="s">Symbol</li>`;
      field.appendChild(rules);
    }

    const bar = $(".bar", meter);
    const update = () => {
      const v = input.value || "";
      const checks = {
        len: v.length >= 8, u: hasU(v), l: hasL(v), d: hasD(v), s: hasS(v)
      };
      const score = Object.values(checks).filter(Boolean).length;
      bar.style.width = `${(score / 5) * 100}%`;
      bar.dataset.level = String(score); // for color via CSS
      $$(".pw-rules li", field).forEach(li => li.classList.toggle("ok", checks[li.dataset.r]));
      setState(input, score >= 3, score >= 3 ? "" : "Use a stronger password.");
      return score >= 3;
    };
    on(input, "input", update);
    return update;
  }

  /* ------------ Email helpers ------------ */
  function attachEmail(input) {
    const field = input.closest(".field");
    const domains = ["gmail.com", "outlook.com", "yahoo.com", "hotmail.com", "icloud.com"];
    const lev = (a, b) => {
      const m = Array.from({ length: b.length + 1 }, (_, i) => [i]);
      for (let j = 0; j <= a.length; j++) m[0][j] = j;
      for (let i = 1; i <= b.length; i++)
        for (let j = 1; j <= a.length; j++)
          m[i][j] = b[i - 1] === a[j - 1] ? m[i - 1][j - 1] : Math.min(m[i - 1][j - 1] + 1, m[i][j - 1] + 1, m[i - 1][j] + 1);
      return m[b.length][a.length];
    };
    const update = () => {
      const v = input.value.trim();
      if (!v) { setState(input, false, "Email is required."); return false; }
      if (!isEmail(v)) { setState(input, false, "Enter a valid email."); return false; }
      const dom = v.split("@")[1]?.toLowerCase() || "";
      const tip = domains.find(d => lev(dom, d) <= 2);
      setState(input, true, tip ? `Did you mean @${tip}?` : "");
      return true;
    };
    on(input, "input", update);
    return update;
  }

  /* ------------ Prevent double-submits + page wiring ------------ */
  $$("form[data-form]").forEach((form) => {
    const type = form.getAttribute("data-form");
    const submitBtn = form.querySelector('button[type="submit"], .btn');
    submitBtn && (submitBtn.dataset.originalText = submitBtn.innerHTML);

    // Page-specific setup
    let gates = [];
    if (type === "register") {
      const name = $('input[name="name"]', form);
      const email = $('input[name="email"]', form);
      const phone = $('input[name="phone"]', form);
      const pass = $('input[name="password"]', form);
      on(phone, "input", () => { phone.value = digits(phone.value).slice(0, 10); });
      gates = [
        () => { const ok = (name.value.trim().length >= 2); setState(name, ok, ok?"":"Enter your full name."); return ok; },
        attachEmail(email),
        () => { const ok = isPhoneIN(phone.value); setState(phone, ok, ok?"":"Enter a valid 10-digit mobile (starts 6–9)."); return ok; },
        attachPassword(pass)
      ];
    }

    if (type === "login") {
      const email = $('input[name="email"]', form);
      const pass = $('input[name="password"]', form);
      gates = [
        () => { const ok = isEmail(email.value); setState(email, ok, ok?"":"Enter a valid email."); return ok; },
        () => { const ok = pass.value.trim().length > 0; setState(pass, ok, ok?"":"Password required."); return ok; }
      ];
    }

    if (type === "deposit" || type === "withdraw") {
      const amt = $('input[name="amount"]', form);
      const banner = $(".otp-banner", form);
      const amtUpdate = attachAmount(amt, banner);
      gates = [amtUpdate];

      // Confirm modal if > 10k
      const modal = $("#otpConfirmModal");
      if (modal) {
        form.addEventListener("submit", (e) => {
          const n = clamp2(parseFloat(amt.value));
          if (!isNaN(n) && n > 10000 && !modal.dataset.ok) {
            e.preventDefault();
            $("#otpConfirmAmount").textContent = INR(n);
            modal.classList.add("show");
          }
        });
        on($("#otpConfirmCancel"), "click", () => modal.classList.remove("show"));
        on($("#otpConfirmYes"), "click", () => { modal.dataset.ok = "1"; modal.classList.remove("show"); form.submit(); });
      }
    }

    if (type === "otp-deposit" || type === "otp-withdraw") {
      // Build advanced OTP grid but still post a single 'otp' string
      const ensureOtp = attachOtpGrid(form);
      gates = [ensureOtp];
    }

    // Submit handling
    form.addEventListener("submit", (e) => {
      if (submitBtn?.dataset.submitting === "1") { e.preventDefault(); return; }
      const ok = gates.every((g) => g && g() !== false);
      if (!ok) { e.preventDefault(); toast("Please correct the highlighted fields.", "error"); return; }
      setLoading(submitBtn, true);
    });
  });

  // expose toast for optional server-sent messages (if you want)
  window.BankUI = { toast };
})();

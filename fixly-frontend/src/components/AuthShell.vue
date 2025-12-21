<template>
  <div class="page auth-shell">
    <div class="bg-shape bg-shape-1"></div>
    <div class="bg-shape bg-shape-2"></div>

    <div class="auth-panel">
      <header class="auth-panel__header auth-panel__header--spaced">
        <div class="auth-panel__text">
          <div class="auth-panel__title">Fixly</div>
        </div>
        <img src="/logo.png" alt="Fixly" class="auth-logo" />
      </header>

      <section class="auth-card auth-panel__card">
        <slot />
      </section>
    </div>
  </div>
</template>

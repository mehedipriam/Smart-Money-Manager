function ComingSoonPage({ title, phase }) {
  return (
    <div className="card" style={{ textAlign: 'center', padding: '48px 24px' }}>
      <h1 style={{ marginTop: 0 }}>{title}</h1>
      <p style={{ color: 'var(--color-text-muted)' }}>
        {title} is planned for {phase} and isn&apos;t built yet.
      </p>
    </div>
  );
}

export default ComingSoonPage;

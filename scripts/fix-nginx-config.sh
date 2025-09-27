#!/bin/bash

echo "🔧 Setting up frontend public directory..."

# Create public directory
mkdir -p frontend/public

# Create favicon.ico (simple 16x16 transparent PNG)
cat > frontend/public/favicon.ico << 'EOF'
AAABAAEAEBAAAAEAIABoBAAAFgAAACgAAAAQAAAAIAAAAAEAIAAAAAAAAAQAABILAAASCwAAAAAAAAAAAAD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AP///wD///8A////AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==
EOF

# Copy logo files if they exist in root
if [ -f "Ankurshala Logo - Watermark (Small) - 300x300.png" ]; then
    echo "🎨 Copying logo files..."
    cp "Ankurshala Logo - Watermark (Small) - 300x300.png" frontend/public/ankurshala-logo.png
fi

# Create a simple SVG logo
cat > frontend/public/ankurshala.svg << 'EOF'
<svg width="300" height="300" viewBox="0 0 300 300" fill="none" xmlns="http://www.w3.org/2000/svg">
  <rect width="300" height="300" rx="50" fill="#2563eb"/>
  <text x="150" y="160" font-family="Arial, sans-serif" font-size="48" font-weight="bold" text-anchor="middle" fill="white">AS</text>
</svg>
EOF

# Create robots.txt
cat > frontend/public/robots.txt << 'EOF'
User-agent: *
Allow: /

Sitemap: https://ankurshala.com/sitemap.xml
EOF

# Create a simple manifest.json
cat > frontend/public/manifest.json << 'EOF'
{
  "name": "AnkurShala",
  "short_name": "AnkurShala",
  "description": "Modern Educational Platform",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#2563eb",
  "icons": [
    {
      "src": "/ankurshala.svg",
      "sizes": "300x300",
      "type": "image/svg+xml"
    }
  ]
}
EOF

echo "✅ Frontend public directory setup complete!"
echo "📁 Contents:"
ls -la frontend/public/
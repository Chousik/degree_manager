# Nginx configuration for frontends

File `frontends.conf` contains two server blocks:

- `fixly-meow.ru` on port **443** (HTTPS) serving the user frontend from `/home/fixly/web/fixly-frontend`.
- `fixly-meow.ru:5050` serving the admin frontend from `/home/fixly/web/fixly-admin`.

Both sites reuse the TLS certificate that already lives on the server at `/opt/registry/certs/`.

## Deploying the config

1. Copy `deployment/nginx/frontends.conf` to the server (e.g. `/etc/nginx/sites-available/frontends.conf`).
2. Ensure the target directories exist:
   ```bash
   sudo mkdir -p /home/fixly/web/fixly-frontend /home/fixly/web/fixly-admin
   ```
3. Enable the site and reload nginx:
   ```bash
   sudo ln -sf /etc/nginx/sites-available/frontends.conf /etc/nginx/sites-enabled/frontends.conf
   sudo nginx -t
   sudo systemctl reload nginx
   ```

The GitHub Actions workflow copies the built bundles into `/home/fixly/web/fixly-frontend` and `/home/fixly/web/fixly-admin` and reloads nginx automatically after deployment.

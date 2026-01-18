не,# Nginx configuration for frontends

File `frontends.conf` contains two server blocks:

- `fixly-meow.ru` on port **443** (HTTPS) serving the user frontend from `/var/www/fixly-frontend`.
- `fixly-meow.ru:5050` serving the admin frontend from `/var/www/fixly-admin`.

Both sites reuse the TLS certificate that already lives on the server at `/opt/registry/certs/`.

## Deploying the config

1. Copy `deployment/nginx/frontends.conf` to the server (e.g. `/etc/nginx/sites-available/frontends.conf`).
2. Ensure the target directories exist:
   ```bash
   sudo mkdir -p /var/www/fixly-frontend /var/www/fixly-admin
   ```
3. Enable the site and reload nginx:
   ```bash
   sudo ln -sf /etc/nginx/sites-available/frontends.conf /etc/nginx/sites-enabled/frontends.conf
   sudo nginx -t
   sudo systemctl reload nginx
   ```

The GitHub Actions workflow copies the built bundles into `/var/www/fixly-frontend` and `/var/www/fixly-admin` and reloads nginx automatically after deployment.

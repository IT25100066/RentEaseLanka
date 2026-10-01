FROM node:22-alpine3.21

WORKDIR /app

# Install dependencies first
COPY package.json package-lock.json ./
RUN npm install

# Copy the rest of the code
COPY . .

# Expose Vite's default port
EXPOSE 5173

# Start the Vite development server and expose it to the network
CMD ["npm", "run", "dev", "--", "--host"]
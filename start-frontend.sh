#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}===========================================${NC}"
echo -e "${BLUE}    Supply Chain Frontend Development    ${NC}"
echo -e "${BLUE}===========================================${NC}"

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo -e "${RED}Error: Node.js is not installed${NC}"
    echo "Please install Node.js 18 or higher from https://nodejs.org/"
    exit 1
fi

# Check Node.js version
NODE_VERSION=$(node --version | cut -d 'v' -f 2 | cut -d '.' -f 1)
if [ "$NODE_VERSION" -lt 18 ]; then
    echo -e "${RED}Error: Node.js version 18 or higher required${NC}"
    echo "Current version: $(node --version)"
    exit 1
fi

# Navigate to frontend directory
cd "$(dirname "$0")/frontend" || exit 1

echo -e "${YELLOW}Checking frontend dependencies...${NC}"

# Install dependencies if node_modules doesn't exist
if [ ! -d "node_modules" ]; then
    echo -e "${YELLOW}Installing dependencies...${NC}"
    npm install
    if [ $? -ne 0 ]; then
        echo -e "${RED}Failed to install dependencies${NC}"
        exit 1
    fi
fi

echo -e "${GREEN}Starting frontend development server...${NC}"
echo -e "${BLUE}Frontend will be available at: http://localhost:3000${NC}"
echo -e "${BLUE}API Gateway should be running at: http://localhost:8080${NC}"
echo ""
echo -e "${YELLOW}Make sure your backend services are running:${NC}"
echo "  ./build.sh dev"
echo ""
echo -e "${YELLOW}Press Ctrl+C to stop the development server${NC}"
echo ""

# Start the development server
npm run dev

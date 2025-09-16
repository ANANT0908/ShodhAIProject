# Shodh Frontend - Updated (Next.js)

This updated frontend implements a modern UI for the judge system and integrates with your backend endpoints:
- Fetch contests: `GET /api/contests`
- Fetch problems for a contest: `GET /api/contests/{contestId}/problems` (or `GET /api/problems?contestId=`)
- Fetch problem detail: `GET /api/problems/{problemId}`
- Submit code: `POST /api/submissions` (body: { contestId, problemId, language, sourceCode })
- Get submission status: `GET /api/submissions/{submissionUuid}`

**Notes**:
- The backend must provide the endpoints above. If your endpoints differ, update `lib/api.js` accordingly.
- The JudgeService must be able to run `shodh-judge:latest` image (build the image added to backend/judge/Dockerfile).
- The DB seeding exists under backend `src/main/resources/initdb/seed.sql` (ensure Postgres init mount or run the SQL).

## Run locally
1. Install dependencies: `npm install`
2. Start dev server: `npm run dev`
3. Open `http://localhost:3000`

## What's included
- `pages/` for routing
- `components/` UI components (CodeEditor, SubmissionStatus, ResultTable)
- `styles/global.css` simple modern styles
- `lib/api.js` central place to change API endpoints

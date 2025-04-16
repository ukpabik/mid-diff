import type { NextApiRequest, NextApiResponse } from "next";

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
  const { puuid } = req.query;

  if (!puuid || typeof puuid !== "string") {
    return res.status(400).json({ error: "Missing or invalid puuid" });
  }

  const response = await fetch(`${process.env.INTERNAL_BACKEND_URL}/user/db/${puuid}`, {
    headers: { "X-API-KEY": process.env.BACKEND_API_KEY as string },
  });

  const data = await response.json();
  res.status(response.status).json(data);
}

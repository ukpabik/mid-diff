import type { NextApiRequest, NextApiResponse } from "next";

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
  const { puuid, region } = req.query;

  if (!puuid || !region || typeof puuid !== "string" || typeof region !== "string") {
    return res.status(400).json({ error: "Missing or invalid puuid or region" });
  }

  const response = await fetch(`${process.env.INTERNAL_BACKEND_URL}/user/rank/${puuid}/${region}`, {
    headers: { "X-API-KEY": process.env.BACKEND_API_KEY as string },
  });

  const data = await response.json();
  res.status(response.status).json(data);
}

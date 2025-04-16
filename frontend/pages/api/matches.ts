import type { NextApiRequest, NextApiResponse } from "next";

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
  const { puuid } = req.query;

  const response = await fetch(`${process.env.INTERNAL_BACKEND_URL}/user/matches/${puuid}`, {
    headers: { "X-API-KEY": process.env.BACKEND_API_KEY as string },
  });

  const data = await response.json();
  res.status(response.status).json(data);
}
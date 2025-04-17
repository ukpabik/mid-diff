import type { NextApiRequest, NextApiResponse } from "next";
import type { PlayerBuild } from "@/lib/types";

export default async function handler(
  req: NextApiRequest,
  res: NextApiResponse<PlayerBuild | { error: string }>
) {

  const { matchId, puuid } = req.query;
  if (typeof matchId !== "string" || typeof puuid !== "string") {
    return res.status(400).json({ error: "Invalid matchId or puuid" });
  }

  try {
    const backendRes = await fetch(
      `${process.env.INTERNAL_BACKEND_URL}/user/build/${matchId}/${puuid}`,
      {
        headers: {
          "X-API-KEY": process.env.BACKEND_API_KEY as string,
        },
      }
    );

    const data = await backendRes.json();
    return res.status(backendRes.status).json(data);
  } catch (err) {
    console.error("Error proxying build request:", err);
    return res.status(500).json({ error: "Internal Server Error" });
  }
}
import type { Player, Match, RankInfoEntry, PlayerBuild } from "./types"

const API_BASE_URL = process.env.NEXT_PUBLIC_BASE_URL || "http://localhost:3000";
const FLASK_API_URL = process.env.NEXT_PUBLIC_FLASK_API_URL || "http://localhost:5000";

export async function searchPlayer(
  riotId: string,
  tagLine: string,
  platformRegion: string,
  routingRegion: string
): Promise<Player> {
  const res = await fetch(
    `${API_BASE_URL}/api/search?riotId=${riotId}&tagLine=${tagLine}&platformRegion=${platformRegion}&routingRegion=${routingRegion}`
  );
  if (!res.ok) throw new Error("Failed to search player");
  return res.json();
}

export async function getPlayerFromDb(puuid: string): Promise<Player> {
  const res = await fetch(`${API_BASE_URL}/api/db?puuid=${puuid}`);
  if (!res.ok) throw new Error("Failed to fetch player from DB");
  const data = await res.json();
  return {
    puuid: data.puuid,
    gameName: data.game_name,
    tagLine: data.tag_line,
    profilePicture: data.profile_picture,
  };
}

export async function getBuildFromDb(matchId: string, puuid: string): Promise<PlayerBuild> {
  const res = await fetch(`${API_BASE_URL}/api/build?matchId=${matchId}&puuid=${puuid}`);
  if (!res.ok) throw new Error("Failed to fetch build info");
  return res.json();
}

export async function getRankInfoFromDb(puuid: string, region: string): Promise<RankInfoEntry[]> {
  const res = await fetch(`${API_BASE_URL}/api/rank?puuid=${puuid}&region=${region}`);
  if (!res.ok) throw new Error("Failed to fetch rank info");
  return res.json();
}

export async function getCachedMatches(puuid: string): Promise<Match[]> {
  const res = await fetch(`${API_BASE_URL}/api/matches?puuid=${puuid}`);
  if (!res.ok) throw new Error("Failed to get cached matches");
  return res.json();
}

export async function analyzeMatch(match: Match) {
  return fetch(`${FLASK_API_URL}/analyze`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      matchId: match.matchId,
      championName: match.championName,
      championId: match.championId,
      kills: match.kills,
      deaths: match.deaths,
      assists: match.assists,
      goldEarned: match.goldEarned,
      goldSpent: match.goldSpent,
      csPerMin: match.csPerMin,
      kda: match.kda,
      visionScore: match.visionScore,
      wardsPlaced: match.wardsPlaced,
      wardsKilled: match.wardsKilled,
      damageDealtToChampions: match.damageDealtToChampions,
      totalDamageTaken: match.totalDamageTaken,
      totalMinionsKilled: match.totalMinionsKilled,
      neutralMinionsKilled: match.neutralMinionsKilled,
      turretTakedowns: match.turretTakedowns,
      inhibitorTakedowns: match.inhibitorTakedowns,
      gameDuration: match.gameDuration,
      win: match.win,
      teamPosition: match.teamPosition,
    }),
  });
}
export interface Player {
  puuid: string
  gameName: string
  tagLine: string
  profilePicture: string
}

export interface Match {
  matchId: string
  puuid: string
  championName: string
  teamPosition: string
  win: boolean
  kills: number
  deaths: number
  assists: number
  goldEarned: number
  goldSpent: number
  totalMinionsKilled: number
  neutralMinionsKilled: number
  damageDealtToChampions: number
  totalDamageTaken: number
  visionScore: number
  wardsPlaced: number
  wardsKilled: number
  turretTakedowns: number
  inhibitorTakedowns: number
  gameStartTimestamp: number
  gameDuration: number
  gameMode: string
  queueId: number
  csPerMin: number
  kda: number
}

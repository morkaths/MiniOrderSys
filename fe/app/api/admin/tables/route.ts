// {project_dir}/fe/app/api/admin/tables/route.ts
import { NextResponse } from "next/server";
import { getAuthTokenFromRequest, proxyToBackend, readJsonRequestBody } from "@/lib/backend";

function missingTokenResponse() {
  return NextResponse.json(
    { message: "Can dang nhap admin de thao tac." },
    { status: 401 }
  );
}

export async function GET(request: Request) {
  const token = getAuthTokenFromRequest(request);
  if (!token) {
    return missingTokenResponse();
  }

  return proxyToBackend({
    method: "GET",
    path: "/api/admin/tables",
    token,
  });
}

export async function POST(request: Request) {
  const token = getAuthTokenFromRequest(request);
  if (!token) {
    return missingTokenResponse();
  }

  const body = await readJsonRequestBody(request);
  if (!body) {
    return NextResponse.json(
      { message: "Payload ban khong hop le." },
      { status: 400 }
    );
  }

  return proxyToBackend({
    method: "POST",
    path: "/api/admin/tables",
    body,
    token,
  });
}

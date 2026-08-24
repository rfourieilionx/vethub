/**
 * Visit API Controller
 *
 * Thin API layer for visit-related operations.
 * Handles HTTP communication only - no business logic.
 */
import { client } from '$lib/api/client';
import type {
	VisitResponse,
	CreateVisitRequest,
	UpdateVisitRequest
} from '$lib/api/models';

/**
 * Get all visits
 */
export async function getVisits(): Promise<VisitResponse[]> {
	const { data, error } = await client.GET('/v1/visits');
	if (error) throw error;
	return data ?? [];
}

/**
 * Get visit by ID
 */
export async function getVisitById(id: number): Promise<VisitResponse> {
	const { data, error } = await client.GET('/v1/visits/{id}', {
		params: { path: { id } }
	});
	if (error) throw error;
	if (!data) throw new Error('Visit not found');
	return data;
}

/**
 * Get visits by pet (for a specific owner's pet)
 */
export async function getVisitsByPet(ownerId: number, petId: number): Promise<VisitResponse[]> {
	const { data, error } = await client.GET('/v1/owners/{ownerId}/pets/{petId}/visits', {
		params: { path: { ownerId, petId } }
	});
	if (error) throw error;
	return data ?? [];
}

/**
 * Get all visits across every pet belonging to an owner, most recent first
 */
export async function getVisitsByOwner(ownerId: number): Promise<VisitResponse[]> {
	const { data, error } = await client.GET('/v1/owners/{ownerId}/visits', {
		params: { path: { ownerId } }
	});
	if (error) throw error;
	return data ?? [];
}

/**
 * Create a new visit
 */
export async function createVisit(request: CreateVisitRequest): Promise<VisitResponse> {
	const { data, error } = await client.POST('/v1/visits', {
		body: request
	});
	if (error) throw error;
	if (!data) throw new Error('Failed to create visit');
	return data;
}

/**
 * Create a new visit for a specific pet
 */
export async function createVisitForPet(
	ownerId: number,
	petId: number,
	request: Omit<CreateVisitRequest, 'petId'>
): Promise<VisitResponse> {
	const { data, error } = await client.POST('/v1/owners/{ownerId}/pets/{petId}/visits', {
		params: { path: { ownerId, petId } },
		body: request
	});
	if (error) throw error;
	if (!data) throw new Error('Failed to create visit');
	return data;
}

/**
 * Update an existing visit
 */
export async function updateVisit(id: number, request: UpdateVisitRequest): Promise<VisitResponse> {
	const { data, error } = await client.PUT('/v1/visits/{id}', {
		params: { path: { id } },
		body: request
	});
	if (error) throw error;
	if (!data) throw new Error('Failed to update visit');
	return data;
}

/**
 * Delete a visit
 */
export async function deleteVisit(id: number): Promise<void> {
	const { error } = await client.DELETE('/v1/visits/{id}', {
		params: { path: { id } }
	});
	if (error) throw error;
}
